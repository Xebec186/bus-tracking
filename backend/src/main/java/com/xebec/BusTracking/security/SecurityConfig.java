package com.xebec.BusTracking.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public tracking read access
                        .requestMatchers(HttpMethod.GET, "/api/tracking/**").permitAll()
                        // Driver actions
                        .requestMatchers(HttpMethod.POST, "/api/tracking/location").hasAuthority("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/buses/*/location").hasAuthority("DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/api/trips/*/status").hasAuthority("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/trips/*/depart").hasAuthority("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/trips/*/arrive").hasAuthority("DRIVER")
                        .requestMatchers(HttpMethod.GET, "/api/trips/driver/*").hasAuthority("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/driver/tickets/*/validate").hasAuthority("DRIVER")
                        // Passenger actions
                        .requestMatchers("/api/passenger/**").hasAuthority("PASSENGER")
                        // Admin actions
                        .requestMatchers(HttpMethod.POST, "/api/buses").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/buses/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/buses/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/routes").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/routes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/routes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/routes/*/stops").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/schedules").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/schedules/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/schedules/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/schedules/*/days").hasAuthority("ADMIN")
                        .requestMatchers("/api/reports/**").hasAuthority("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // This explicitly sends 401 instead of the default 403/302
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**")
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login", "/logout").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // Admin UI
                        .requestMatchers(
                                "/dashboard",
                                "/buses",
                                "/routes/**",
                                "/stops/**",
                                "/schedules/**",
                                "/tickets/**",
                                "/tracking/**",
                                "/reports/**"
                        ).hasAuthority("ADMIN")
                        .requestMatchers("/users/**").hasAuthority("ADMIN")
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler)
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**"))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
       DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
       provider.setPasswordEncoder(passwordEncoder());
       return provider;
    }
}
