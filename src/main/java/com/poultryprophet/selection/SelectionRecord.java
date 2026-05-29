package com.poultryprophet.selection;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.bird.Bird;
import com.poultryprophet.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Blueprint 5.2 Stage 3 / Module 3: the manager's month-5 confirm-or-override decision for a
 * bird. The system suggests advancement when CRS meets the cut-line; the manager confirms or
 * overrides, and an override reason is recorded as research data. One-to-one with {@link Bird}.
 */
@Entity
@Table(name = "selection_record")
public class SelectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bird_id", nullable = false, unique = true)
    private Bird bird;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    /** CRS at the moment of the decision (snapshot, for auditability). */
    @Column(nullable = false)
    private double crsAtDecision;

    /** What the system suggested (CRS met the cut-line). */
    @Column(nullable = false)
    private boolean recommendedAdvance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SelectionOutcome outcome;

    /** True when the manager's outcome differs from the system recommendation. */
    @Column(nullable = false)
    private boolean overridden;

    /** Required when {@link #overridden} is true; recorded as research data. */
    @Column(columnDefinition = "text")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decided_by", nullable = false)
    private User decidedBy;

    @Column(nullable = false)
    private Instant decidedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bird getBird() {
        return bird;
    }

    public void setBird(Bird bird) {
        this.bird = bird;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public double getCrsAtDecision() {
        return crsAtDecision;
    }

    public void setCrsAtDecision(double crsAtDecision) {
        this.crsAtDecision = crsAtDecision;
    }

    public boolean isRecommendedAdvance() {
        return recommendedAdvance;
    }

    public void setRecommendedAdvance(boolean recommendedAdvance) {
        this.recommendedAdvance = recommendedAdvance;
    }

    public SelectionOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(SelectionOutcome outcome) {
        this.outcome = outcome;
    }

    public boolean isOverridden() {
        return overridden;
    }

    public void setOverridden(boolean overridden) {
        this.overridden = overridden;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(User decidedBy) {
        this.decidedBy = decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
