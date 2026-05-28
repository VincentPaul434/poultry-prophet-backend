package com.poultryprophet.record;

import com.poultryprophet.record.dto.CreateRecordRequest;
import com.poultryprophet.record.dto.DailyRecordResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/batches/{batchId}/records")
public class DailyRecordController {

    private final DailyRecordService recordService;

    public DailyRecordController(DailyRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public ResponseEntity<DailyRecordResponse> create(@PathVariable Long batchId,
                                                      @Valid @RequestBody CreateRecordRequest request,
                                                      @AuthenticationPrincipal CustomUserDetails principal) {
        DailyRecordResponse response =
                recordService.record(batchId, principal.getFarmId(), request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<DailyRecordResponse> recent(@PathVariable Long batchId,
                                            @RequestParam(defaultValue = "14") int limit,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        return recordService.recent(batchId, principal.getFarmId(), limit);
    }
}
