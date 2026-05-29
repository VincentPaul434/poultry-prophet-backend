package com.poultryprophet.bird;

import com.poultryprophet.batch.Batch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Blueprint 5.4: an individual bird, banded around week 4 when ranging begins. Birds belong
 * to a batch; each bird inherits its batch's brooding score and carries its own growth, health
 * and behavioural scores from the ranging stage (the batch-vs-individual hybrid model).
 */
@Entity
@Table(name = "bird", uniqueConstraints = @UniqueConstraint(columnNames = {"batch_id", "band_number"}))
public class Bird {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    /** Leg-band identifier, unique within the batch. */
    @Column(name = "band_number", nullable = false)
    private String bandNumber;

    /** Optional free-text note (e.g. wing colour, distinguishing marks). */
    @Column
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getBandNumber() {
        return bandNumber;
    }

    public void setBandNumber(String bandNumber) {
        this.bandNumber = bandNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
