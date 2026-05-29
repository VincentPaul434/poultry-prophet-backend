package com.poultryprophet.ranging;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Blueprint 5.2 Stage 2 / 6.1: a weekly ranging milestone for an individual bird. Captures the
 * sampled weight, any health event that week, a temperament observation, and the breeders'
 * own C/B+/A/A++ quality rating. Feeds the per-bird growth, health-history and behavioural
 * sub-scores. Idempotent per (bird, record_date).
 */
@Entity
@Table(name = "ranging_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bird_id", "record_date"}))
public class RangingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bird_id", nullable = false)
    private Bird bird;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "handler_id", nullable = false)
    private User handler;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /** Average weight of the sampled/banded bird that week, in grams. */
    @Column(nullable = false)
    private double weightG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthEventSeverity healthEvent = HealthEventSeverity.NONE;

    @Column(columnDefinition = "text")
    private String temperamentNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualityRating qualityRating;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

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

    public User getHandler() {
        return handler;
    }

    public void setHandler(User handler) {
        this.handler = handler;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public double getWeightG() {
        return weightG;
    }

    public void setWeightG(double weightG) {
        this.weightG = weightG;
    }

    public HealthEventSeverity getHealthEvent() {
        return healthEvent;
    }

    public void setHealthEvent(HealthEventSeverity healthEvent) {
        this.healthEvent = healthEvent;
    }

    public String getTemperamentNotes() {
        return temperamentNotes;
    }

    public void setTemperamentNotes(String temperamentNotes) {
        this.temperamentNotes = temperamentNotes;
    }

    public QualityRating getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(QualityRating qualityRating) {
        this.qualityRating = qualityRating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
