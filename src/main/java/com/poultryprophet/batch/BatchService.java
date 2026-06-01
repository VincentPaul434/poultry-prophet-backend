package com.poultryprophet.batch;

import com.poultryprophet.batch.dto.BatchResponse;
import com.poultryprophet.batch.dto.BatchTrackingResponse;
import com.poultryprophet.batch.dto.CreateBatchRequest;
import com.poultryprophet.batch.dto.StageTrackerItem;
import com.poultryprophet.common.BadRequestException;
import com.poultryprophet.common.NotFoundException;
import com.poultryprophet.user.Role;
import com.poultryprophet.user.User;
import com.poultryprophet.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SDD 1.3: business rules for batch registration — name uniqueness per farm, stage
 * validation, and atomic creation of handler assignments.
 */
@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final LifecycleStageRepository stageRepository;
    private final BatchHandlerAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public BatchService(BatchRepository batchRepository,
                        LifecycleStageRepository stageRepository,
                        BatchHandlerAssignmentRepository assignmentRepository,
                        UserRepository userRepository) {
        this.batchRepository = batchRepository;
        this.stageRepository = stageRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BatchResponse create(CreateBatchRequest request, Long farmId) {
        if (batchRepository.existsByFarmIdAndNameIgnoreCase(farmId, request.name())) {
            throw new BadRequestException("A batch named '" + request.name() + "' already exists on this farm");
        }
        LifecycleStage stage = stageRepository.findById(request.stageId())
                .orElseThrow(() -> new BadRequestException("Unknown lifecycle stage id " + request.stageId()));

        Batch batch = new Batch();
        batch.setFarmId(farmId);
        batch.setName(request.name());
        batch.setInitialPopulation(request.initialPopulation());
        batch.setCurrentPopulation(request.initialPopulation());
        batch.setStartDate(request.startDate());
        batch.setBloodline(request.bloodline());
        batch.setSource(request.source());
        batch.setStage(stage);
        batch.setStatus(BatchStatus.ACTIVE);
        batchRepository.save(batch);

        List<Long> handlerIds = request.handlerUserIds() == null ? List.of() : request.handlerUserIds();
        for (Long handlerId : handlerIds) {
            User handler = userRepository.findById(handlerId)
                    .orElseThrow(() -> new BadRequestException("Unknown handler id " + handlerId));
            if (handler.getRole() != Role.HANDLER) {
                throw new BadRequestException("User " + handlerId + " is not a handler");
            }
            if (!handler.getFarmId().equals(farmId)) {
                throw new BadRequestException("Handler " + handlerId + " belongs to a different farm");
            }
            assignmentRepository.save(new BatchHandlerAssignment(batch, handler));
        }

        return BatchResponse.from(batch, new ArrayList<>(handlerIds));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> listForFarm(Long farmId) {
        List<BatchResponse> result = new ArrayList<>();
        for (Batch batch : batchRepository.findByFarmIdOrderByCreatedAtDesc(farmId)) {
            result.add(BatchResponse.from(batch, assignmentRepository.findHandlerUserIdsByBatchId(batch.getId())));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public BatchResponse getForFarm(Long batchId, Long farmId) {
        Batch batch = requireBatch(batchId, farmId);
        return BatchResponse.from(batch, assignmentRepository.findHandlerUserIdsByBatchId(batch.getId()));
    }

    /**
     * Advances (or moves) a batch to another lifecycle stage. Needed for the lifecycle
     * progression brooding -> ranging -> ... required by the selection workflow.
     */
    @Transactional
    public BatchResponse changeStage(Long batchId, Long farmId, Long stageId) {
        Batch batch = requireBatch(batchId, farmId);
        LifecycleStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new BadRequestException("Unknown lifecycle stage id " + stageId));
        batch.setStage(stage);
        batchRepository.save(batch);
        return BatchResponse.from(batch, assignmentRepository.findHandlerUserIdsByBatchId(batchId));
    }

    @Transactional(readOnly = true)
    public BatchTrackingResponse getTracking(Long batchId, Long farmId) {
        Batch batch = requireBatch(batchId, farmId);
        long daysElapsed = Math.max(1, ChronoUnit.DAYS.between(batch.getStartDate(), LocalDate.now()));
        DevelopmentStage current = DevelopmentStage.fromDaysElapsed(daysElapsed);

        List<DevelopmentStage> tracked = Arrays.asList(
                DevelopmentStage.BROODING, DevelopmentStage.RANGING, DevelopmentStage.SELECTION);

        List<StageTrackerItem> tracker = tracked.stream().map(stage -> {
            String status;
            if (stage.isBefore(current)) status = "COMPLETED";
            else if (stage == current) status = "ACTIVE";
            else status = "UPCOMING";
            return new StageTrackerItem(
                    stage, stage.getDisplayName(), stage.getStartDay(), stage.getEndDay(), status);
        }).toList();

        return new BatchTrackingResponse(
                batch.getId(), batch.getName(), batch.getStartDate(),
                daysElapsed, current, current.getDisplayName(), tracker);
    }

    /** Shared accessor used by record/analytics/report flows to enforce farm scoping. */
    @Transactional(readOnly = true)
    public Batch requireBatch(Long batchId, Long farmId) {
        return batchRepository.findByIdAndFarmId(batchId, farmId)
                .orElseThrow(() -> new NotFoundException("Batch " + batchId + " not found"));
    }
}
