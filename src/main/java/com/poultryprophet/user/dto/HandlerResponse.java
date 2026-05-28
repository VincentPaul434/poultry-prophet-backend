package com.poultryprophet.user.dto;

import com.poultryprophet.user.User;

public record HandlerResponse(
        Long id,
        String email,
        String fullName
) {
    public static HandlerResponse from(User user) {
        return new HandlerResponse(user.getId(), user.getEmail(), user.getFullName());
    }
}
