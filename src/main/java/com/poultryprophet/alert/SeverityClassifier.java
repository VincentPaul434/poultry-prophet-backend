package com.poultryprophet.alert;

import com.poultryprophet.analytics.ThresholdConfig;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SDD 2.3: classifies an indicator value against its threshold band. Returns empty when the
 * value is in range; otherwise WARNING when it is just outside the band and CRITICAL when it
 * is well outside. Direction-agnostic, so it works for "too low" (BHI) and "too high" (BSI).
 */
@Component
public class SeverityClassifier {

    private static final double WARNING_MARGIN_FRACTION = 0.15;

    public Optional<Severity> classify(double value, ThresholdConfig threshold) {
        double min = threshold.getMinValue();
        double max = threshold.getMaxValue();
        if (value >= min && value <= max) {
            return Optional.empty();
        }
        double distance = value < min ? (min - value) : (value - max);
        double band = Math.max(1.0, (max - min) * WARNING_MARGIN_FRACTION);
        return Optional.of(distance <= band ? Severity.WARNING : Severity.CRITICAL);
    }
}
