package com.poultryprophet.scoring;

import com.poultryprophet.ranging.HealthEventSeverity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration-driven tuning for the Conditioning Readiness scoring engine (Blueprint 6).
 * Bound from {@code poultry.scoring.*}. Per the blueprint's honesty rule, every value here is a
 * documented, ADJUSTABLE starting value - not an established fact. Adjustability is itself a
 * feature: weights can be tuned as real data accrues.
 */
@ConfigurationProperties(prefix = "poultry.scoring")
public class ScoringProperties {

    private CrsWeights crsWeights = new CrsWeights();
    private BhiWeights bhiWeights = new BhiWeights();
    private double mortalityBestPct = 1.0;
    private double mortalityWorstPct = 5.0;
    private double growthPenaltyPerPct = 2.0;
    private HealthDeduction healthDeduction = new HealthDeduction();
    private List<GrowthPoint> expectedWeight = defaultCurve();
    private double cutLineCrs = 70.0;

    private static List<GrowthPoint> defaultCurve() {
        List<GrowthPoint> curve = new ArrayList<>();
        curve.add(new GrowthPoint(4, 300.0));
        curve.add(new GrowthPoint(8, 700.0));
        curve.add(new GrowthPoint(12, 1100.0));
        curve.add(new GrowthPoint(16, 1500.0));
        curve.add(new GrowthPoint(20, 1900.0));
        return curve;
    }

    /** Point deduction for a health event of the given severity. */
    public double deductionFor(HealthEventSeverity severity) {
        if (severity == null) {
            return 0.0;
        }
        return switch (severity) {
            case NONE -> 0.0;
            case ROUTINE -> healthDeduction.getRoutine();
            case MINOR -> healthDeduction.getMinor();
            case MODERATE -> healthDeduction.getModerate();
            case MAJOR -> healthDeduction.getMajor();
        };
    }

    /**
     * Expected weight (grams) for an age in weeks, via linear interpolation between the
     * configured curve points. Ages below/above the curve clamp to the first/last point.
     */
    public double expectedWeightForWeek(double weeks) {
        if (expectedWeight == null || expectedWeight.isEmpty()) {
            return 0.0;
        }
        List<GrowthPoint> points = expectedWeight.stream()
                .sorted((a, b) -> Integer.compare(a.getWeek(), b.getWeek()))
                .toList();
        GrowthPoint first = points.get(0);
        if (weeks <= first.getWeek()) {
            return first.getGrams();
        }
        GrowthPoint last = points.get(points.size() - 1);
        if (weeks >= last.getWeek()) {
            return last.getGrams();
        }
        for (int i = 0; i < points.size() - 1; i++) {
            GrowthPoint lo = points.get(i);
            GrowthPoint hi = points.get(i + 1);
            if (weeks >= lo.getWeek() && weeks <= hi.getWeek()) {
                double span = hi.getWeek() - lo.getWeek();
                double t = span == 0 ? 0 : (weeks - lo.getWeek()) / span;
                return lo.getGrams() + t * (hi.getGrams() - lo.getGrams());
            }
        }
        return last.getGrams();
    }

    public CrsWeights getCrsWeights() {
        return crsWeights;
    }

    public void setCrsWeights(CrsWeights crsWeights) {
        this.crsWeights = crsWeights;
    }

    public BhiWeights getBhiWeights() {
        return bhiWeights;
    }

    public void setBhiWeights(BhiWeights bhiWeights) {
        this.bhiWeights = bhiWeights;
    }

    public double getMortalityBestPct() {
        return mortalityBestPct;
    }

    public void setMortalityBestPct(double mortalityBestPct) {
        this.mortalityBestPct = mortalityBestPct;
    }

    public double getMortalityWorstPct() {
        return mortalityWorstPct;
    }

    public void setMortalityWorstPct(double mortalityWorstPct) {
        this.mortalityWorstPct = mortalityWorstPct;
    }

    public double getGrowthPenaltyPerPct() {
        return growthPenaltyPerPct;
    }

    public void setGrowthPenaltyPerPct(double growthPenaltyPerPct) {
        this.growthPenaltyPerPct = growthPenaltyPerPct;
    }

    public HealthDeduction getHealthDeduction() {
        return healthDeduction;
    }

    public void setHealthDeduction(HealthDeduction healthDeduction) {
        this.healthDeduction = healthDeduction;
    }

    public List<GrowthPoint> getExpectedWeight() {
        return expectedWeight;
    }

    public void setExpectedWeight(List<GrowthPoint> expectedWeight) {
        this.expectedWeight = expectedWeight;
    }

    public double getCutLineCrs() {
        return cutLineCrs;
    }

    public void setCutLineCrs(double cutLineCrs) {
        this.cutLineCrs = cutLineCrs;
    }

    /** CRS composite weights (Blueprint 6.2). */
    public static class CrsWeights {
        private double broodingHealth = 0.30;
        private double growth = 0.30;
        private double healthHistory = 0.20;
        private double behavioural = 0.20;

        public double total() {
            return broodingHealth + growth + healthHistory + behavioural;
        }

        public double getBroodingHealth() {
            return broodingHealth;
        }

        public void setBroodingHealth(double broodingHealth) {
            this.broodingHealth = broodingHealth;
        }

        public double getGrowth() {
            return growth;
        }

        public void setGrowth(double growth) {
            this.growth = growth;
        }

        public double getHealthHistory() {
            return healthHistory;
        }

        public void setHealthHistory(double healthHistory) {
            this.healthHistory = healthHistory;
        }

        public double getBehavioural() {
            return behavioural;
        }

        public void setBehavioural(double behavioural) {
            this.behavioural = behavioural;
        }
    }

    /** Brooding Health Index sub-weights (Blueprint 6.1). */
    public static class BhiWeights {
        private double mortality = 0.50;
        private double intake = 0.30;
        private double temperature = 0.20;

        public double total() {
            return mortality + intake + temperature;
        }

        public double getMortality() {
            return mortality;
        }

        public void setMortality(double mortality) {
            this.mortality = mortality;
        }

        public double getIntake() {
            return intake;
        }

        public void setIntake(double intake) {
            this.intake = intake;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

    /** Health-history deductions per event severity (Blueprint 6.1). */
    public static class HealthDeduction {
        private double routine = 0.0;
        private double minor = 5.0;
        private double moderate = 15.0;
        private double major = 30.0;

        public double getRoutine() {
            return routine;
        }

        public void setRoutine(double routine) {
            this.routine = routine;
        }

        public double getMinor() {
            return minor;
        }

        public void setMinor(double minor) {
            this.minor = minor;
        }

        public double getModerate() {
            return moderate;
        }

        public void setModerate(double moderate) {
            this.moderate = moderate;
        }

        public double getMajor() {
            return major;
        }

        public void setMajor(double major) {
            this.major = major;
        }
    }

    /** One point on the expected ranging weight curve. */
    public static class GrowthPoint {
        private int week;
        private double grams;

        public GrowthPoint() {
        }

        public GrowthPoint(int week, double grams) {
            this.week = week;
            this.grams = grams;
        }

        public int getWeek() {
            return week;
        }

        public void setWeek(int week) {
            this.week = week;
        }

        public double getGrams() {
            return grams;
        }

        public void setGrams(double grams) {
            this.grams = grams;
        }
    }
}
