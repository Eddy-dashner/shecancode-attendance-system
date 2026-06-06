package com.shecancode.attendence.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final AuthEntryPoint authEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    // ── Public endpoints (no token required) ─────────────────────────────────
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",          // login & register
            "/h2-console/**",           // dev H2 console
            "/swagger-ui/**",           // Swagger UI
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // H2 console needs frames; CSRF is irrelevant for stateless JWT APIs
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()))

            // Stateless — no HttpSession
            .sessionManagement(sess -> sess
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // ── Public ──────────────────────────────────────────────────
                .requestMatchers(PUBLIC_URLS).permitAll()

                // ── Cohort management: ADMIN only ───────────────────────────
                .requestMatchers("/api/v1/cohorts/**").hasAuthority("ROLE_ADMIN")

                // ── Program management: ADMIN only ──────────────────────────
                .requestMatchers("/api/v1/programs/**").hasRole("ADMIN")

                // ── Student registration: ADMIN only ────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/v1/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")

                // ── Student lifecycle (drop-out, reinstate): ADMIN only ──────
                .requestMatchers("/api/v1/students/lifecycle/**").hasRole("ADMIN")

                // ── Read students: ADMIN or TRAINER ─────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/students/**")
                        .hasAnyRole("ADMIN", "TRAINER")

                // ── Attendance recording: ADMIN or TRAINER ───────────────────
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/programs/*/cohorts/*/attendance/**")
                        .hasAnyRole("ADMIN", "TRAINER")
                .requestMatchers(HttpMethod.PATCH,
                        "/api/v1/programs/*/cohorts/*/attendance/**")
                        .hasAnyRole("ADMIN", "TRAINER")

                // ── Attendance read: all authenticated users ─────────────────
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/programs/*/cohorts/*/attendance/**")
                        .authenticated()

                // ── User management: ADMIN only ──────────────────────────────
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                // ── Everything else requires authentication ──────────────────
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }

    // ── Beans ─────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
