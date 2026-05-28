package com.poultryprophet.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** SDD 1.2 data design: audit/retry log of offline sync submissions. */
@Entity
@Table(name = "sync_attempt")
public class SyncAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Resolved server-side record id, when one was written or matched. */
    @Column(name = "record_id")
    private Long recordId;

    @Column(nullable = false)
    private Instant attemptAt = Instant.now();

    @Column(nullable = false)
    private int httpStatus;

    @Column(name = "error_code")
    private String errorCode;

    public SyncAttempt() {
    }

    public SyncAttempt(Long recordId, int httpStatus, String errorCode) {
        this.recordId = recordId;
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Instant getAttemptAt() {
        return attemptAt;
    }

    public void setAttemptAt(Instant attemptAt) {
        this.attemptAt = attemptAt;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
