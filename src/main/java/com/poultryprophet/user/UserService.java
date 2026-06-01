package com.poultryprophet.user;

import com.poultryprophet.common.ConflictException;
import com.poultryprophet.user.dto.CreateHandlerRequest;
import com.poultryprophet.user.dto.HandlerResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public HandlerResponse createHandler(CreateHandlerRequest request, Long farmId) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(Role.HANDLER);
        user.setFarmId(farmId);
        userRepository.save(user);
        return HandlerResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<HandlerResponse> listHandlers(Long farmId) {
        return userRepository.findByRoleAndFarmId(Role.HANDLER, farmId).stream()
                .map(HandlerResponse::from)
                .toList();
    }
}
