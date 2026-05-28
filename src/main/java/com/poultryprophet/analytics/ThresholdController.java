package com.poultryprophet.analytics;

import com.poultryprophet.analytics.dto.ThresholdResponse;
import com.poultryprophet.analytics.dto.UpdateThresholdRequest;
import com.poultryprophet.common.BadRequestException;
import com.poultryprophet.common.NotFoundException;
import com.poultryprophet.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SDD 2.4: thresholds are DB-backed and editable without code changes. */
@RestController
@RequestMapping("/api/thresholds")
public class ThresholdController {

    private final ThresholdConfigRepository thresholdRepository;

    public ThresholdController(ThresholdConfigRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    @GetMapping
    public List<ThresholdResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return thresholdRepository.findByFarmId(principal.getFarmId()).stream()
                .map(ThresholdResponse::from)
                .toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ThresholdResponse update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateThresholdRequest request,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        if (request.minValue() > request.maxValue()) {
            throw new BadRequestException("minValue must not exceed maxValue");
        }
        ThresholdConfig threshold = thresholdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Threshold " + id + " not found"));
        if (threshold.getFarmId() == null || !threshold.getFarmId().equals(principal.getFarmId())) {
            throw new BadRequestException("Cannot edit a threshold outside your farm");
        }
        threshold.setMinValue(request.minValue());
        threshold.setMaxValue(request.maxValue());
        return ThresholdResponse.from(thresholdRepository.save(threshold));
    }
}
