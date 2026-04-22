package com.xebec.BusTracking.security;

import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Long userId = ((MyUserDetails) authentication.getPrincipal()).getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id: " + userId));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String redirectUrl = user.getRole() == UserRole.ADMIN
                ? "/dashboard"
                : "/login?error=forbidden";

        response.sendRedirect(redirectUrl);
    }
}
