package com.poultryprophet.invite;

import com.poultryprophet.auth.AuthService;
import com.poultryprophet.auth.dto.AuthResponse;
import com.poultryprophet.common.BadRequestException;
import com.poultryprophet.security.CustomUserDetails;
import com.poultryprophet.user.User;
import com.poultryprophet.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;
    private final UserRepository userRepository;
    private final AuthService authService;

    public InviteController(InviteService inviteService,
                            UserRepository userRepository,
                            AuthService authService) {
        this.inviteService = inviteService;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<InviteResponse> create(@Valid @RequestBody CreateInviteRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails principal) {
        InviteResponse response = inviteService.createHandlerInvite(request.email(), principal.getFarmId(), request.expiresInDays());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{token}/accept")
    @PreAuthorize("hasRole('HANDLER')")
    public ResponseEntity<AuthResponse> accept(@PathVariable String token,
                                               @AuthenticationPrincipal CustomUserDetails principal) {
        Long farmId = inviteService.acceptInvite(token, principal.getUsername());
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setFarmId(farmId);
        userRepository.save(user);
        return ResponseEntity.ok(authService.toResponse(user));
    }
}