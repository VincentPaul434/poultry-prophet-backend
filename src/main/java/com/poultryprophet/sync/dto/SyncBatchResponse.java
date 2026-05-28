package com.poultryprophet.sync.dto;

import java.util.List;

public record SyncBatchResponse(
        int total,
        int synced,
        int conflicts,
        int failed,
        List<SyncItemResult> results
) {
}
