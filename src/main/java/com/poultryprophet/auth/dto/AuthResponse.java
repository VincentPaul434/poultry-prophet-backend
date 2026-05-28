package com.poultryprophet.auth.dto;

import com.poultryprophet.user.Role;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String fullName,
        Role role,
        Long farmId
) {
}
