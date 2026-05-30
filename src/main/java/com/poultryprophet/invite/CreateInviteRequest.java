package com.poultryprophet.invite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateInviteRequest(
        @NotBlank @Email String email,
        @Min(1) int expiresInDays
) {
}