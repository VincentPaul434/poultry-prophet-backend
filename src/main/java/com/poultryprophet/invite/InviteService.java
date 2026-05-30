package com.poultryprophet.invite;

import com.poultryprophet.common.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class InviteService {

    private final HandlerInviteRepository inviteRepository;

    public InviteService(HandlerInviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    @Transactional
    public InviteResponse createHandlerInvite(String email, Long farmId, int expiresInDays) {
        HandlerInvite invite = new HandlerInvite();
        invite.setToken(UUID.randomUUID().toString());
        invite.setEmail(email);
        invite.setFarmId(farmId);
        invite.setExpiresAt(Instant.now().plus(expiresInDays, ChronoUnit.DAYS));
        inviteRepository.save(invite);

        return new InviteResponse(invite.getToken(), invite.getEmail(), invite.getFarmId(), invite.getExpiresAt());
    }

    @Transactional
    public Long acceptInvite(String token, String email) {
        HandlerInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid invite token"));

        if (!invite.getEmail().equalsIgnoreCase(email)) {
            throw new BadRequestException("Invite token does not match the email address");
        }

        if (invite.getUsedAt() != null) {
            throw new BadRequestException("Invite token has already been used");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invite token has expired");
        }

        invite.setUsedAt(Instant.now());
        inviteRepository.save(invite);
        return invite.getFarmId();
    }
}