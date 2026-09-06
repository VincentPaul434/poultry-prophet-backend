package com.poultryprophet.intervention;

import com.poultryprophet.alert.Alert;
import com.poultryprophet.alert.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InterventionRecommendationCatalogTest {

    private final InterventionRecommendationCatalog catalog = new InterventionRecommendationCatalog();

    @Test
    void createsCriticalBroodingChecklistWithManagerReview() {
        Alert alert = alert("BHI", Severity.CRITICAL);

        InterventionRecommendationCatalog.Recommendation recommendation = catalog.recommend(alert);

        assertThat(recommendation.title()).isEqualTo("Check batch brooding conditions");
        assertThat(recommendation.instructions())
                .contains("Notify the farm manager")
                .contains("temperature")
                .contains("water access");
        assertThat(recommendation.managerReviewRequired()).isTrue();
    }

    @Test
    void createsWaterAndFeedChecklistWithoutAutomaticTreatmentAdvice() {
        Alert alert = alert("WFR", Severity.WARNING);

        InterventionRecommendationCatalog.Recommendation recommendation = catalog.recommend(alert);

        assertThat(recommendation.title()).isEqualTo("Inspect feed and water systems");
        assertThat(recommendation.instructions())
                .contains("drinkers")
                .contains("feeders")
                .doesNotContain("medication")
                .doesNotContain("dosage");
        assertThat(recommendation.managerReviewRequired()).isFalse();
    }

    private Alert alert(String indicatorType, Severity severity) {
        Alert alert = new Alert();
        alert.setIndicatorType(indicatorType);
        alert.setSeverity(severity);
        return alert;
    }
}
