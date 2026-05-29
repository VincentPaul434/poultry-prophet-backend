package com.poultryprophet.ranging;

/**
 * Blueprint 6.1 (Health History sub-score): severity of a health event logged in a weekly
 * ranging milestone. The point deduction per severity is configuration-driven
 * (see {@code poultry.scoring.health-deduction}); defaults are routine 0, minor -5,
 * moderate -15, major -30. NONE means no event was logged that week.
 */
public enum HealthEventSeverity {
    NONE,
    ROUTINE,
    MINOR,
    MODERATE,
    MAJOR
}
