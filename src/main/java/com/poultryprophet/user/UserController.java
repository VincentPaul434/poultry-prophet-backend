package com.poultryprophet.user;

import com.poultryprophet.security.CustomUserDetails;
import com.poultryprophet.user.dto.HandlerResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Backs SDD 1.3 HandlerMultiSelect: lists handlers for the manager's farm. */
@RestController
@RequestMapping("/api/handlers")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<HandlerResponse> listHandlers(@AuthenticationPrincipal CustomUserDetails principal) {
        return userRepository.findByRoleAndFarmId(Role.HANDLER, principal.getFarmId()).stream()
                .map(HandlerResponse::from)
                .toList();
    }
}
