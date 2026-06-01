package com.poultryprophet.batch;

public enum DevelopmentStage {
    BROODING("Brooding & Chick Stage", 1, 30),
    RANGING("Range & Growing Stage", 31, 120),
    SELECTION("Pre-Selection & Final Evaluation", 121, 150),
    COMPLETED("Lifecycle Completed", 151, Integer.MAX_VALUE);

    private final String displayName;
    private final int startDay;
    private final int endDay;

    DevelopmentStage(String displayName, int startDay, int endDay) {
        this.displayName = displayName;
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public String getDisplayName() { return displayName; }
    public int getStartDay() { return startDay; }
    public int getEndDay() { return endDay; }

    public static DevelopmentStage fromDaysElapsed(long days) {
        if (days <= 30) return BROODING;
        if (days <= 120) return RANGING;
        if (days <= 150) return SELECTION;
        return COMPLETED;
    }

    public boolean isBefore(DevelopmentStage other) {
        return this.ordinal() < other.ordinal();
    }
}
