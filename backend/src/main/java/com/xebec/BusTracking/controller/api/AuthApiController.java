package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.TokenResponse;
import com.xebec.BusTracking.dto.request.ChangePasswordRequest;
import com.xebec.BusTracking.dto.request.RefreshTokenRequest;
import com.xebec.BusTracking.dto.user.LoginDto;
import com.xebec.BusTracking.dto.user.SignupDto;
import com.xebec.BusTracking.dto.user.UserDto;
import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.repository.UserRepository;
import com.xebec.BusTracking.security.MyUserDetails;
import com.xebec.BusTracking.service.UserService;
import com.xebec.BusTracking.service.impl.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

    @PostMapping("/signup")
    public TokenResponse signup(@Valid @RequestBody SignupDto signupDto) {
        User user = userService.signup(signupDto);
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new TokenResponse(token, refreshToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String email = jwtService.extractUsername(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!jwtService.validateToken(request.getRefreshToken(), new MyUserDetails(user))) {
            return ResponseEntity.badRequest().build();
        }
        String token = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(token, newRefreshToken));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal MyUserDetails principal,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        User user = principal.getUser();
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Incorrect current password");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "New passwords do not match");
            return ResponseEntity.badRequest().body(error);
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}
