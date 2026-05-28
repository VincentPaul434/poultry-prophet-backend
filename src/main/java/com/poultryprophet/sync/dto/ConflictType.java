package com.poultryprophet.sync.dto;

/** Classification produced by the ConflictResolver (SDD 1.2). */
public enum ConflictType {
    /** No server record for (batch, date) yet — accept as new. */
    NEW,
    /** Server already has an identical record — treat as already synced. */
    IDENTICAL,
    /** Client copy is newer than the server copy — overwrite. */
    LOCAL_NEWER,
    /** Server copy is newer — surface a conflict for the handler to resolve. */
    SERVER_NEWER
}
