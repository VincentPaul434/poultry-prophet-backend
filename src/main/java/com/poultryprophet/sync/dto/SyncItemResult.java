package com.poultryprophet.sync.dto;

/** Per-item acknowledgment returned by POST /api/sync/batch. */
public record SyncItemResult(
        String clientId,
        String status,        // "synced" | "conflict" | "failed"
        Long recordId,
        ConflictType conflictType,
        String message
) {
    public static SyncItemResult synced(String clientId, Long recordId, ConflictType type) {
        return new SyncItemResult(clientId, "synced", recordId, type, null);
    }

    public static SyncItemResult conflict(String clientId, Long recordId, ConflictType type) {
        return new SyncItemResult(clientId, "conflict", recordId, type,
                "Server copy is newer; manual resolution required");
    }

    public static SyncItemResult failed(String clientId, String message) {
        return new SyncItemResult(clientId, "failed", null, null, message);
    }
}
