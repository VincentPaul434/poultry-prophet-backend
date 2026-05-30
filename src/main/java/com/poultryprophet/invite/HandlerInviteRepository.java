package com.poultryprophet.invite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HandlerInviteRepository extends JpaRepository<HandlerInvite, Long> {

    Optional<HandlerInvite> findByToken(String token);
}