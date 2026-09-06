package com.poultryprophet.intervention;

import com.poultryprophet.alert.Alert;
import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchHandlerAssignment;
import com.poultryprophet.batch.BatchHandlerAssignmentRepository;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.common.BadRequestException;
import com.poultryprophet.common.ConflictException;
import com.poultryprophet.common.NotFoundException;
import com.poultryprophet.intervention.dto.AssignInterventionRequest;
import com.poultryprophet.intervention.dto.InterventionActionRequest;
import com.poultryprophet.intervention.dto.InterventionHistoryResponse;
import com.poultryprophet.intervention.dto.InterventionResponse;
import com.poultryprophet.realtime.RealtimeNotificationService;
import com.poultryprophet.user.Role;
import com.poultryprophet.user.User;
import com.poultryprophet.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final InterventionHistoryRepository historyRepository;
    private final BatchHandlerAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final BatchService batchService;
    private final InterventionRecommendationCatalog catalog;
    private final RealtimeNotificationService realtime;

    public InterventionService(InterventionRepository interventionRepository,
                                InterventionHistoryRepository historyRepository,
                                BatchHandlerAssignmentRepository assignmentRepository,
                                UserRepository userRepository,
                                BatchService batchService,
                                InterventionRecommendationCatalog catalog,
                                RealtimeNotificationService realtime) {
        this.interventionRepository = interventionRepository;
        this.historyRepository = historyRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.batchService = batchService;
        this.catalog = catalog;
        this.realtime = realtime;
    }

    /** Creates one operational recommendation for an alert. Safe to call repeatedly. */
    @Transactional
    public InterventionResponse createForAlert(Alert alert) {
        return interventionRepository.findByAlertId(alert.getId())
                .map(InterventionResponse::from)
                .orElseGet(() -> {
                    InterventionRecommendationCatalog.Recommendation recommendation = catalog.recommend(alert);
                    Intervention intervention = new Intervention();
                    intervention.setAlert(alert);
                    intervention.setBatch(alert.getBatch());
                    intervention.setIndicatorType(alert.getIndicatorType());
                    intervention.setSeverity(alert.getSeverity());
                    intervention.setTitle(recommendation.title());
                    intervention.setInstructions(recommendation.instructions());
                    intervention.setManagerReviewRequired(recommendation.managerReviewRequired());
                    Intervention saved = interventionRepository.save(intervention);
                    audit(saved, null, "CREATED", "Generated from alert " + alert.getId());
                    realtime.publishInterventionUpdated(saved);
                    return InterventionResponse.from(saved);
                });
    }

    @Transactional(readOnly = true)
    public List<InterventionResponse> listForActor(Long farmId, Long userId, Role role,
                                                   String statusText, int limit) {
        InterventionStatus status = parseStatus(statusText);
        PageRequest page = PageRequest.of(0, boundedLimit(limit));
        List<Intervention> interventions;
        if (role == Role.MANAGER) {
            interventions = status == null
                    ? interventionRepository.findByBatch_FarmIdOrderByCreatedAtDesc(farmId, page)
                    : interventionRepository.findByBatch_FarmIdAndStatusOrderByCreatedAtDesc(farmId, status, page);
        } else {
            List<Long> batchIds = assignmentRepository.findByUserId(userId).stream()
                    .map(BatchHandlerAssignment::getBatch)
                    .filter(batch -> farmId.equals(batch.getFarmId()))
                    .map(Batch::getId)
                    .toList();
            if (batchIds.isEmpty()) {
                return List.of();
            }
            interventions = status == null
                    ? interventionRepository.findByBatchIdInOrderByCreatedAtDesc(batchIds, page)
                    : interventionRepository.findByBatchIdInAndStatusOrderByCreatedAtDesc(batchIds, status, page);
        }
        return interventions.stream().map(InterventionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InterventionResponse> listForBatch(Long batchId, Long farmId, Long userId,
                                                   Role role, String statusText, int limit) {
        Batch batch = batchService.requireBatch(batchId, farmId);
        assertBatchViewAccess(batch, userId, role);
        InterventionStatus status = parseStatus(statusText);
        PageRequest page = PageRequest.of(0, boundedLimit(limit));
        List<Intervention> interventions = status == null
                ? interventionRepository.findByBatchIdOrderByCreatedAtDesc(batchId, page)
                : interventionRepository.findByBatchIdAndStatusOrderByCreatedAtDesc(batchId, status, page);
        return interventions.stream().map(InterventionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public InterventionResponse get(Long interventionId, Long farmId, Long userId, Role role) {
        return InterventionResponse.from(requireForActor(interventionId, farmId, userId, role));
    }

    @Transactional(readOnly = true)
    public List<InterventionHistoryResponse> history(Long interventionId, Long farmId,
                                                     Long userId, Role role) {
        Intervention intervention = requireForActor(interventionId, farmId, userId, role);
        return historyRepository.findByInterventionIdOrderByCreatedAtAsc(intervention.getId()).stream()
                .map(InterventionHistoryResponse::from)
                .toList();
    }

    @Transactional
    public InterventionResponse claim(Long interventionId, Long farmId, Long handlerId) {
        Intervention intervention = requireForHandler(interventionId, farmId, handlerId, false);
        if (intervention.getAssignedTo() != null) {
            if (handlerId.equals(intervention.getAssignedTo().getId())) {
                return InterventionResponse.from(intervention);
            }
            throw new ConflictException("This intervention is already assigned to another handler");
        }
        if (intervention.getStatus() != InterventionStatus.PENDING) {
            throw new ConflictException("Only pending interventions can be claimed");
        }
        User handler = userRepository.getReferenceById(handlerId);
        intervention.setAssignedTo(handler);
        intervention.setStatus(InterventionStatus.ACKNOWLEDGED);
        intervention.setAcknowledgedAt(Instant.now());
        Intervention saved = interventionRepository.save(intervention);
        audit(saved, handlerId, "CLAIMED", null);
        realtime.publishInterventionUpdated(saved);
        return InterventionResponse.from(saved);
    }

    @Transactional
    public InterventionResponse start(Long interventionId, Long farmId, Long handlerId) {
        Intervention intervention = requireForHandler(interventionId, farmId, handlerId, true);
        requireStatus(intervention, InterventionStatus.PENDING, InterventionStatus.ACKNOWLEDGED);
        Instant now = Instant.now();
        if (intervention.getAcknowledgedAt() == null) {
            intervention.setAcknowledgedAt(now);
        }
        intervention.setStartedAt(now);
        intervention.setStatus(InterventionStatus.IN_PROGRESS);
        Intervention saved = interventionRepository.save(intervention);
        audit(saved, handlerId, "STARTED", null);
        realtime.publishInterventionUpdated(saved);
        return InterventionResponse.from(saved);
    }

    @Transactional
    public InterventionResponse complete(Long interventionId, Long farmId, Long handlerId,
                                         InterventionActionRequest request) {
        Intervention intervention = requireForHandler(interventionId, farmId, handlerId, true);
        requireStatus(intervention, InterventionStatus.ACKNOWLEDGED, InterventionStatus.IN_PROGRESS);
        Instant now = Instant.now();
        intervention.setStatus(InterventionStatus.COMPLETED);
        intervention.setCompletedAt(now);
        intervention.setResolvedBy(userRepository.getReferenceById(handlerId));
        intervention.setOutcomeNote(note(request));
        Intervention saved = interventionRepository.save(intervention);
        audit(saved, handlerId, "COMPLETED", note(request));
        realtime.publishInterventionUpdated(saved);
        return InterventionResponse.from(saved);
    }

    @Transactional
    public InterventionResponse escalate(Long interventionId, Long farmId, Long handlerId,
                                         InterventionActionRequest request) {
        Intervention intervention = requireForHandler(interventionId, farmId, handlerId, true);
        requireStatus(intervention, InterventionStatus.ACKNOWLEDGED, InterventionStatus.IN_PROGRESS);
        String note = requiredNote(request, "An escalation note is required");
        intervention.setStatus(InterventionStatus.ESCALATED);
        intervention.setManagerReviewRequired(true);
        intervention.setEscalatedAt(Instant.now());
        intervention.setOutcomeNote(note);
        Intervention saved = interventionRepository.save(intervention);
        audit(saved, handlerId, "ESCALATED", note);
        realtime.publishInterventionUpdated(saved);
        return InterventionResponse.from(saved);
    }

    @Transactional
    public InterventionResponse assign(Long interventionId, Long farmId, Long managerId,
                                       AssignInterventionRequest request) {
        Intervention intervention = requireForManager(interventionId, farmId);
        if (isTerminal(intervention.getStatus())) {
            throw new ConflictException("A completed or dismissed intervention cannot be assigned");
        }
        if (intervention.getStatus() == InterventionStatus.IN_PROGRESS) {
            throw new ConflictException("An in-progress intervention cannot be reassigned");
        }
        User handler = userRepository.findById(request.handlerUserId())
                .orElseThrow(() -> new BadRequestException("Handler " + request.handlerUserId() + " not found"));
        if (handler.getRole() != Role.HANDLER || !farmId.equals(handler.getFarmId())
                || !assignmentRepository.existsByBatchIdAndUserId(intervention.getBatch().getId(), handler.getId())) {
            throw new BadRequestException("Handler must belong to this farm and be assigned to the batch");
        }
        intervention.setAssignedTo(handler);
        if (intervention.getStatus() == InterventionStatus.ESCALATED) {
            intervention.setStatus(InterventionStatus.PENDING);
            intervention.setEscalatedAt(null);
        }
        Intervention saved = interventionRepository.save(intervention);
        audit(saved, managerId, "ASSIGNED", "Assigned to handler " + handler.getId());
        realtime.publishInterventionUpdated(saved);
        return InterventionResponse.from(saved);
    }

    @Transactional
    public InterventionResponse dismiss(Long interventionId, Long farmId, Long managerId,
                                        InterventionActionRequest request) {
        Intervention intervention = requireForManager(interventionId, farmId);
        if (isTerminal(intervention.getStatus())) {
            throw new ConflictException("This intervention is already closed");
        }
        String note = requiredNote(request, "A dismissal reason is required");
        intervention.setStatus(InterventionStatus.DISMISSED);
        intervention.setDismissedAt(Instant.now());
        intervention.setResolvedBy(userRepository.getReferenceById(managerId));
        intervention.setOutcomeNote(note);
        Intervention saved = interventionRepository.save(intervention);
        audit(saved, managerId, "DISMISSED", note);
        realtime.publishInterventionUpdated(saved);
        return InterventionResponse.from(saved);
    }

    private Intervention requireForActor(Long interventionId, Long farmId, Long userId, Role role) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new NotFoundException("Intervention " + interventionId + " not found"));
        if (!farmId.equals(intervention.getBatch().getFarmId())) {
            throw new NotFoundException("Intervention " + interventionId + " not found");
        }
        if (role == Role.HANDLER && !assignmentRepository.existsByBatchIdAndUserId(
                intervention.getBatch().getId(), userId)) {
            throw new NotFoundException("Intervention " + interventionId + " not found");
        }
        return intervention;
    }

    private Intervention requireForHandler(Long interventionId, Long farmId, Long handlerId,
                                           boolean mustOwn) {
        Intervention intervention = requireForActor(interventionId, farmId, handlerId, Role.HANDLER);
        if (mustOwn && (intervention.getAssignedTo() == null
                || !handlerId.equals(intervention.getAssignedTo().getId()))) {
            throw new ConflictException("Claim this intervention before performing this action");
        }
        return intervention;
    }

    private Intervention requireForManager(Long interventionId, Long farmId) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new NotFoundException("Intervention " + interventionId + " not found"));
        if (!farmId.equals(intervention.getBatch().getFarmId())) {
            throw new NotFoundException("Intervention " + interventionId + " not found");
        }
        return intervention;
    }

    private void assertBatchViewAccess(Batch batch, Long userId, Role role) {
        if (role == Role.HANDLER && !assignmentRepository.existsByBatchIdAndUserId(batch.getId(), userId)) {
            throw new NotFoundException("Batch " + batch.getId() + " not found");
        }
    }

    private void requireStatus(Intervention intervention, InterventionStatus... allowed) {
        for (InterventionStatus candidate : allowed) {
            if (intervention.getStatus() == candidate) {
                return;
            }
        }
        throw new ConflictException("Intervention is not in an actionable state");
    }

    private boolean isTerminal(InterventionStatus status) {
        return status == InterventionStatus.COMPLETED || status == InterventionStatus.DISMISSED;
    }

    private InterventionStatus parseStatus(String statusText) {
        if (!StringUtils.hasText(statusText)) {
            return null;
        }
        try {
            return InterventionStatus.valueOf(statusText.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown intervention status " + statusText);
        }
    }

    private int boundedLimit(int limit) {
        return Math.min(Math.max(1, limit), 200);
    }

    private String note(InterventionActionRequest request) {
        return request == null ? null : request.note();
    }

    private String requiredNote(InterventionActionRequest request, String message) {
        String note = note(request);
        if (!StringUtils.hasText(note)) {
            throw new BadRequestException(message);
        }
        return note.trim();
    }

    private void audit(Intervention intervention, Long actorId, String action, String note) {
        InterventionHistory history = new InterventionHistory();
        history.setIntervention(intervention);
        history.setActor(actorId == null ? null : userRepository.getReferenceById(actorId));
        history.setStatus(intervention.getStatus());
        history.setAction(action);
        history.setNote(note);
        historyRepository.save(history);
    }
}
