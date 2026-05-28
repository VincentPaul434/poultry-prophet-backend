package com.poultryprophet.dashboard;

import com.poultryprophet.dashboard.dto.BatchOverviewResponse;
import com.poultryprophet.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batches/{batchId}")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/overview")
    public BatchOverviewResponse overview(@PathVariable Long batchId,
                                          @AuthenticationPrincipal CustomUserDetails principal) {
        return overviewService.getOverview(batchId, principal.getFarmId());
    }
}
