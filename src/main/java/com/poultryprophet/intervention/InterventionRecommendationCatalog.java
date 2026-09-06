package com.poultryprophet.intervention;

import com.poultryprophet.alert.Alert;
import com.poultryprophet.alert.Severity;
import org.springframework.stereotype.Component;

/**
 * Safe, deterministic intervention playbooks. These are operational checks, not veterinary
 * diagnoses or treatment instructions.
 */
@Component
public class InterventionRecommendationCatalog {

    public Recommendation recommend(Alert alert) {
        String indicator = alert.getIndicatorType();
        return switch (indicator) {
            case "BHI" -> new Recommendation(
                    "Check batch brooding conditions",
                    criticalPrefix(alert.getSeverity())
                            + "Inspect brooder temperature, ventilation, litter, feed access, and water access. "
                            + "Record what you observe and escalate if mortality or distress continues.",
                    alert.getSeverity() == Severity.CRITICAL);
            case "BSI" -> new Recommendation(
                    "Inspect bird behaviour and environment",
                    criticalPrefix(alert.getSeverity())
                            + "Observe the affected birds for unusual behaviour, crowding, lethargy, or distress. "
                            + "Check the surrounding environment and record the observation for manager review.",
                    alert.getSeverity() == Severity.CRITICAL);
            case "WFR" -> new Recommendation(
                    "Inspect feed and water systems",
                    criticalPrefix(alert.getSeverity())
                            + "Inspect drinkers and feeders for blockage, leakage, contamination, or poor access. "
                            + "Verify the latest feed and water readings after the check.",
                    alert.getSeverity() == Severity.CRITICAL);
            default -> new Recommendation(
                    "Review the affected batch",
                    criticalPrefix(alert.getSeverity())
                            + "Review the latest batch records, inspect the birds and environment, record your findings, "
                            + "and notify the manager if the issue persists.",
                    alert.getSeverity() == Severity.CRITICAL);
        };
    }

    private String criticalPrefix(Severity severity) {
        return severity == Severity.CRITICAL
                ? "Notify the farm manager before taking any high-risk action. "
                : "";
    }

    public record Recommendation(String title, String instructions, boolean managerReviewRequired) {
    }
}
