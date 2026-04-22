package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.TokenResponse;
import com.xebec.BusTracking.dto.request.RefreshTokenRequest;
import com.xebec.BusTracking.dto.user.LoginDto;
import com.xebec.BusTracking.dto.user.SignupDto;
import com.xebec.BusTracking.exception.PasswordsDoNotMatchException;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.repository.UserRepository;
import com.xebec.BusTracking.service.UserService;
import com.xebec.BusTracking.service.impl.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupDto signupDto) {
        if(!signupDto.getPassword().equals(signupDto.getConfirmPassword())) {
            throw new PasswordsDoNotMatchException();
        }
        User savedUser = userService.signup(signupDto);
        String token = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        return ResponseEntity.ok(new TokenResponse(token, refreshToken));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        String token = jwtService.generateToken(updatedUser);
        String refreshToken = jwtService.generateRefreshToken(updatedUser);
        return ResponseEntity.ok(new TokenResponse(token, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) throws BadRequestException {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("Invalid refresh token");
        }
        String token = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(token, newRefreshToken));
    }
}