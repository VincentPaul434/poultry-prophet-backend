package com.poultryprophet.analytics;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.record.DailyRecord;
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

/** SDD 2.1: computed indicators for a daily record (one-to-one with DailyRecord). */
@Entity
@Table(name = "indicator")
public class Indicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false, unique = true)
    private DailyRecord record;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(nullable = false)
    private double bhi;

    /** Only populated while the batch is in the brooding stage. */
    private Double bsi;

    /** Null when the day's feed intake was zero (anomalous). */
    private Double wfr;

    @Column(nullable = false)
    private double readinessScore;

    @Column(nullable = false)
    private Instant computedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DailyRecord getRecord() {
        return record;
    }

    public void setRecord(DailyRecord record) {
        this.record = record;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public double getBhi() {
        return bhi;
    }

    public void setBhi(double bhi) {
        this.bhi = bhi;
    }

    public Double getBsi() {
        return bsi;
    }

    public void setBsi(Double bsi) {
        this.bsi = bsi;
    }

    public Double getWfr() {
        return wfr;
    }

    public void setWfr(Double wfr) {
        this.wfr = wfr;
    }

    public double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public Instant getComputedAt() {
        return computedAt;
    }

    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}
