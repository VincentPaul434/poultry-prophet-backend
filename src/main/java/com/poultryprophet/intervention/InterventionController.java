package com.poultryprophet.intervention;

import com.poultryprophet.intervention.dto.AssignInterventionRequest;
import com.poultryprophet.intervention.dto.InterventionActionRequest;
import com.poultryprophet.intervention.dto.InterventionHistoryResponse;
import com.poultryprophet.intervention.dto.InterventionResponse;
import com.poultryprophet.security.CustomUserDetails;
import com.poultryprophet.user.Role;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @GetMapping("/interventions")
    public List<InterventionResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.listForActor(
                principal.getFarmId(), principal.getId(), role(principal), status, limit);
    }

    @GetMapping("/batches/{batchId}/interventions")
    public List<InterventionResponse> listForBatch(
            @PathVariable Long batchId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.listForBatch(
                batchId, principal.getFarmId(), principal.getId(), role(principal), status, limit);
    }

    @GetMapping("/interventions/{id}")
    public InterventionResponse get(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.get(id, principal.getFarmId(), principal.getId(), role(principal));
    }

    @GetMapping("/interventions/{id}/history")
    public List<InterventionHistoryResponse> history(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.history(id, principal.getFarmId(), principal.getId(), role(principal));
    }

    @PostMapping("/interventions/{id}/claim")
    @PreAuthorize("hasRole('HANDLER')")
    public InterventionResponse claim(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.claim(id, principal.getFarmId(), principal.getId());
    }

    @PostMapping("/interventions/{id}/start")
    @PreAuthorize("hasRole('HANDLER')")
    public InterventionResponse start(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.start(id, principal.getFarmId(), principal.getId());
    }

    @PostMapping("/interventions/{id}/complete")
    @PreAuthorize("hasRole('HANDLER')")
    public InterventionResponse complete(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) InterventionActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.complete(id, principal.getFarmId(), principal.getId(), request);
    }

    @PostMapping("/interventions/{id}/escalate")
    @PreAuthorize("hasRole('HANDLER')")
    public InterventionResponse escalate(
            @PathVariable Long id,
            @Valid @RequestBody InterventionActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.escalate(id, principal.getFarmId(), principal.getId(), request);
    }

    @PutMapping("/interventions/{id}/assignment")
    @PreAuthorize("hasRole('MANAGER')")
    public InterventionResponse assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignInterventionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.assign(id, principal.getFarmId(), principal.getId(), request);
    }

    @PostMapping("/interventions/{id}/dismiss")
    @PreAuthorize("hasRole('MANAGER')")
    public InterventionResponse dismiss(
            @PathVariable Long id,
            @Valid @RequestBody InterventionActionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return interventionService.dismiss(id, principal.getFarmId(), principal.getId(), request);
    }

    private Role role(CustomUserDetails principal) {
        return principal.getUser().getRole();
    }
}
