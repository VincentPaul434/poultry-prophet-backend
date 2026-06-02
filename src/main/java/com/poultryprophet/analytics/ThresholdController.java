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

    @GetMapping
    public List<ThresholdResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        // Start from the global defaults (farmId null), then let any farm-specific
        // rows override them per indicator, so every farm sees a full set even
        // before it has customised anything.
        Map<String, ThresholdConfig> effective = new LinkedHashMap<>();
        thresholdRepository.findByFarmIdIsNull()
                .forEach(t -> effective.put(t.getIndicator(), t));
        Long farmId = principal.getFarmId();
        if (farmId != null) {
            thresholdRepository.findByFarmId(farmId)
                    .forEach(t -> effective.put(t.getIndicator(), t));
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
        Long farmId = principal.getFarmId();
        if (farmId == null) {
            throw new BadRequestException("You must belong to a farm to edit thresholds");
        }
        ThresholdConfig threshold = thresholdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Threshold " + id + " not found"));

        // A farm-specific row is edited in place. Editing a global default instead
        // creates (or updates) this farm's own override for that indicator —
        // copy-on-write — leaving the shared global untouched.
        ThresholdConfig target;
        if (farmId.equals(threshold.getFarmId())) {
            target = threshold;
        } else if (threshold.getFarmId() == null) {
            target = thresholdRepository
                    .findByFarmIdAndIndicator(farmId, threshold.getIndicator())
                    .orElseGet(() -> new ThresholdConfig(farmId, threshold.getIndicator(),
                            threshold.getMinValue(), threshold.getMaxValue()));
        } else {
            throw new BadRequestException("Cannot edit a threshold outside your farm");
        }

        target.setMinValue(request.minValue());
        target.setMaxValue(request.maxValue());
        return ThresholdResponse.from(thresholdRepository.save(target));
    }
}
