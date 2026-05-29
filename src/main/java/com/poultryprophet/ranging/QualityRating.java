package com.poultryprophet.ranging;

/**
 * Blueprint 6.1 (Behavioural sub-score): the C / B / B+ / A / A+ / A++ conformation rubric
 * breeders already use. Each grade maps to points (0-100); the behavioural sub-score is the
 * average of these across the bird's weekly ranging milestones.
 */
public enum QualityRating {
    C(20),
    B(40),
    B_PLUS(60),
    A(80),
    A_PLUS(90),
    A_PLUS_PLUS(100);

    private final int points;

    QualityRating(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
