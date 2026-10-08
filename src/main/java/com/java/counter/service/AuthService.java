package com.java.counter.service;

import com.java.counter.dto.AuthResponse;
import com.java.counter.dto.LoginRequest;
import com.java.counter.dto.SignupRequest;
import com.java.counter.entity.DateCount;
import com.java.counter.entity.User;
import com.java.counter.repository.UserRepository;
import com.java.counter.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void signup(SignupRequest request) {
        userRepository.findByEmail(request.email())
                .map(userResult -> { throw new BadCredentialsException("Email đã được sử dụng trước đó! Vui lòng chọn email khác!"); });

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setFullName(request.name());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));

        userRepository.save(newUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token);
    }
}