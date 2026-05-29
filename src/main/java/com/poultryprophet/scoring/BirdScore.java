package com.poultryprophet.scoring;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.bird.Bird;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Blueprint 6: the four sub-scores and the composite Conditioning Readiness Score (CRS) for a
 * single bird. One-to-one with {@link Bird}; recomputed on demand. Persisting the breakdown
 * (not just the CRS) keeps every ranking interrogable/transparent.
 */
@Entity
@Table(name = "bird_score")
public class BirdScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bird_id", nullable = false, unique = true)
    private Bird bird;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    /** Brooding Health Index, inherited from the bird's batch (0-100). */
    @Column(nullable = false)
    private double broodingHealthIndex;

    /** Per-bird growth trajectory vs the expected curve (0-100). */
    @Column(nullable = false)
    private double growthScore;

    /** Per-bird health robustness from logged ranging health events (0-100). */
    @Column(nullable = false)
    private double healthHistoryScore;

    /** Per-bird behavioural score from the C/B+/A/A++ ratings (0-100). */
    @Column(nullable = false)
    private double behaviouralScore;

    /** Composite Conditioning Readiness Score (0-100). */
    @Column(nullable = false)
    private double crs;

    @Column(nullable = false)
    private Instant computedAt = Instant.now();

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

    public double getBroodingHealthIndex() {
        return broodingHealthIndex;
    }

    public void setBroodingHealthIndex(double broodingHealthIndex) {
        this.broodingHealthIndex = broodingHealthIndex;
    }

    public double getGrowthScore() {
        return growthScore;
    }

    public void setGrowthScore(double growthScore) {
        this.growthScore = growthScore;
    }

    public double getHealthHistoryScore() {
        return healthHistoryScore;
    }

    public void setHealthHistoryScore(double healthHistoryScore) {
        this.healthHistoryScore = healthHistoryScore;
    }

    public double getBehaviouralScore() {
        return behaviouralScore;
    }

    public void setBehaviouralScore(double behaviouralScore) {
        this.behaviouralScore = behaviouralScore;
    }

    public double getCrs() {
        return crs;
    }

    public void setCrs(double crs) {
        this.crs = crs;
    }

    public Instant getComputedAt() {
        return computedAt;
    }

    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}
