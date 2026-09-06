package com.poultryprophet.intervention;

import com.poultryprophet.alert.Alert;
import com.poultryprophet.alert.Severity;
import com.poultryprophet.batch.Batch;
import com.poultryprophet.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/** A trackable farm action generated from an alert. */
@Entity
@Table(name = "intervention", indexes = {
        @Index(name = "idx_intervention_batch_status", columnList = "batch_id, status"),
        @Index(name = "idx_intervention_assigned_status", columnList = "assigned_to, status"),
        @Index(name = "idx_intervention_created", columnList = "created_at")
})
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false, unique = true)
    private Alert alert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(nullable = false)
    private String indicatorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterventionStatus status = InterventionStatus.PENDING;

    @Column(nullable = false)
    private boolean managerReviewRequired;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "outcome_note", columnDefinition = "text")
    private String outcomeNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alert getAlert() { return alert; }
    public void setAlert(Alert alert) { this.alert = alert; }
    public Batch getBatch() { return batch; }
    public void setBatch(Batch batch) { this.batch = batch; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    public String getIndicatorType() { return indicatorType; }
    public void setIndicatorType(String indicatorType) { this.indicatorType = indicatorType; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public InterventionStatus getStatus() { return status; }
    public void setStatus(InterventionStatus status) { this.status = status; }
    public boolean isManagerReviewRequired() { return managerReviewRequired; }
    public void setManagerReviewRequired(boolean managerReviewRequired) { this.managerReviewRequired = managerReviewRequired; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(Instant escalatedAt) { this.escalatedAt = escalatedAt; }
    public Instant getDismissedAt() { return dismissedAt; }
    public void setDismissedAt(Instant dismissedAt) { this.dismissedAt = dismissedAt; }
    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getOutcomeNote() { return outcomeNote; }
    public void setOutcomeNote(String outcomeNote) { this.outcomeNote = outcomeNote; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
