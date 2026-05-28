package com.poultryprophet.sync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SyncBatchRequest(
        @NotEmpty @Valid List<SyncItemRequest> records
) {
}
