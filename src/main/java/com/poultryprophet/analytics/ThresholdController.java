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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** SDD 2.4: thresholds are DB-backed and editable without code changes. */
@RestController
@RequestMapping("/api/thresholds")
public class ThresholdController {

    private final ThresholdConfigRepository thresholdRepository;

    public ThresholdController(ThresholdConfigRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    /**
     * Effective thresholds for the caller's farm: the global defaults, each overridden by a
     * farm-specific row where one exists. A fresh farm sees the seeded defaults (marked global)
     * so a manager always has something to adjust.
     */
    @GetMapping
    public List<ThresholdResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        Map<String, ThresholdConfig> effective = new LinkedHashMap<>();
        for (ThresholdConfig global : thresholdRepository.findByFarmIdIsNull()) {
            effective.put(global.getIndicator(), global);
        }
        for (ThresholdConfig farmSpecific : thresholdRepository.findByFarmId(principal.getFarmId())) {
            effective.put(farmSpecific.getIndicator(), farmSpecific); // farm override wins
        }
        return effective.values().stream().map(ThresholdResponse::from).toList();
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
        Long farmId = principal.getFarmId();

        // Editing a global default → copy-on-write a farm-specific override (reusing one if it
        // already exists for this indicator) so the shared default is never mutated.
        if (threshold.getFarmId() == null) {
            ThresholdConfig farmRow = thresholdRepository
                    .findByFarmIdAndIndicator(farmId, threshold.getIndicator())
                    .orElseGet(() -> new ThresholdConfig(
                            farmId, threshold.getIndicator(), request.minValue(), request.maxValue()));
            farmRow.setMinValue(request.minValue());
            farmRow.setMaxValue(request.maxValue());
            return ThresholdResponse.from(thresholdRepository.save(farmRow));
        }

        // Editing an existing farm row → it must belong to the caller's farm.
        if (!threshold.getFarmId().equals(farmId)) {
            throw new BadRequestException("Cannot edit a threshold outside your farm");
        }
        threshold.setMinValue(request.minValue());
        threshold.setMaxValue(request.maxValue());
        return ThresholdResponse.from(thresholdRepository.save(threshold));
    }
}
