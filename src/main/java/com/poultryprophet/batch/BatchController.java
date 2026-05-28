package com.poultryprophet.batch;

import com.poultryprophet.batch.dto.BatchResponse;
import com.poultryprophet.batch.dto.CreateBatchRequest;
import com.poultryprophet.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BatchResponse> create(@Valid @RequestBody CreateBatchRequest request,
                                                @AuthenticationPrincipal CustomUserDetails principal) {
        BatchResponse response = batchService.create(request, principal.getFarmId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<BatchResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return batchService.listForFarm(principal.getFarmId());
    }

    @GetMapping("/{id}")
    public BatchResponse get(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return batchService.getForFarm(id, principal.getFarmId());
    }
}
