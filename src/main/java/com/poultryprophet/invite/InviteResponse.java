package com.poultryprophet.invite;

import java.time.Instant;

public record InviteResponse(
        String token,
        String email,
        Long farmId,
        Instant expiresAt
) {
}