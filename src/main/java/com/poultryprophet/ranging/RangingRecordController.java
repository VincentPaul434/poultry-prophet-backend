package com.poultryprophet.ranging;

import com.poultryprophet.ranging.dto.CreateRangingRecordRequest;
import com.poultryprophet.ranging.dto.RangingRecordResponse;
import com.poultryprophet.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/batches/{batchId}/birds/{birdId}/ranging")
public class RangingRecordController {

    private final RangingRecordService rangingService;

    public RangingRecordController(RangingRecordService rangingService) {
        this.rangingService = rangingService;
    }

    @PostMapping
    public ResponseEntity<RangingRecordResponse> create(@PathVariable Long batchId,
                                                        @PathVariable Long birdId,
                                                        @Valid @RequestBody CreateRangingRecordRequest request,
                                                        @AuthenticationPrincipal CustomUserDetails principal) {
        RangingRecordResponse response =
                rangingService.record(batchId, birdId, principal.getFarmId(), request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<RangingRecordResponse> list(@PathVariable Long batchId,
                                            @PathVariable Long birdId,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        return rangingService.listForBird(batchId, birdId, principal.getFarmId());
    }
}
