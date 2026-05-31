package com.poultryprophet.invite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HandlerInviteRepository extends JpaRepository<HandlerInvite, Long> {

    Optional<HandlerInvite> findByToken(String token);

    /** Pending invites for an email: not accepted, not declined and not expired. */
    List<HandlerInvite> findByEmailIgnoreCaseAndUsedAtIsNullAndDeclinedAtIsNullAndExpiresAtAfter(
            String email, Instant now);
}