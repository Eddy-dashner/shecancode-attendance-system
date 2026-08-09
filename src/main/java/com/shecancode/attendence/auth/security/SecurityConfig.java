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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtAuthenticationFilter jwtAuthFilter;
//    private final AuthenticationProvider authenticationProvider;
////    private final AuthenticationEntryPoint authEntryPoint;
//    private final AccessDeniedHandler accessDeniedHandler;;
//
//    // ── ADD THIS LINE ──────────────────────────────────────────────────
//    private final UserDetailsService userDetailsService;
//    // ───────────────────────────────────────────────────────────────────
//
//    private final PasswordEncoder passwordEncoder;
//
//    // ── Public endpoints (no token required) ─────────────────────────────────
//    private static final String[] PUBLIC_URLS = {
//            "/api/v1/auth/**",          // login & register
//            "/h2-console/**",           // dev H2 console
//            "/swagger-ui/**",           // Swagger UI
//            "/swagger-ui.html",
//            "/v3/api-docs/**",
//            "/actuator/health"
//    };
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Disable CSRF (Stateless REST API handles token-based safety)
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // 2. Clear out frame-options constraints safely for internal tool visibility (e.g. H2 console)
//                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
//
//                // 3. Keep application completely stateless (No HttpSessions created)
//                .sessionManagement(sess -> sess
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // 4. Fine-grained Access Control Mapping
//                .authorizeHttpRequests(auth -> auth
//
//                        // ── Public Access ───────────────────────────────────────────
//                        .requestMatchers(PUBLIC_URLS).permitAll()
//
//                        // ── Cohort & Program Structures (ADMIN exclusive) ───────────
//                        .requestMatchers("/api/v1/cohorts/**").hasRole("ADMIN")
//                        .requestMatchers("/api/v1/programs/**").hasRole("ADMIN")
//
//                        // ── Student Mutation Management (ADMIN exclusive) ───────────
//                        .requestMatchers(HttpMethod.POST, "/api/v1/students/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
//                        .requestMatchers("/api/v1/students/lifecycle/**").hasRole("ADMIN")
//
//                        // ── Student Readings (Elevated Staff Access) ────────────────
//                        .requestMatchers(HttpMethod.GET, "/api/v1/students/**")
//                        .hasAnyRole("ADMIN", "TRAINER")
//
//                        // ── Attendance Submissions (Elevated Staff Access) ─────────
//                        .requestMatchers(HttpMethod.POST, "/api/v1/programs/*/cohorts/*/attendance/**")
//                        .hasAnyRole("ADMIN", "TRAINER")
//                        .requestMatchers(HttpMethod.PATCH, "/api/v1/programs/*/cohorts/*/attendance/**")
//                        .hasAnyRole("ADMIN", "TRAINER")
//
//                        // ── Attendance Extractions (Any Verified Token) ─────────────
//                        .requestMatchers(HttpMethod.GET, "/api/v1/programs/*/cohorts/*/attendance/**")
//                        .authenticated()
//
//                        // ── Core Account Adjustments (ADMIN exclusive) ──────────────
//                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
//
//                        // ── Default Safe Catch-all Fallback ──────────────────────────
//                        .anyRequest().authenticated()
//                )
//
//                // 5. Wire custom backing authentication provider and order execution pipelines
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//
//                // 6. Handle failure delegation smoothly back to application JSON exceptions
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(authEntryPoint)
//                        .accessDeniedHandler(accessDeniedHandler)
//                );
//        return http.build();
//    }
//
//    // ── Beans ─────────────────────────────────────────────────────────────────
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService); // This will now resolve perfectly!
//        provider.setPasswordEncoder(passwordEncoder);
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//

private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationEntryPoint authEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/h2-console/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()

                        .requestMatchers("/api/v1/cohorts/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/programs/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/students/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/students/lifecycle/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/students/**")
                        .hasAnyRole("ADMIN", "TRAINER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/programs/*/cohorts/*/attendance/**")
                        .hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/programs/*/cohorts/*/attendance/**")
                        .hasAnyRole("ADMIN", "TRAINER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/programs/*/cohorts/*/attendance/**")
                        .authenticated()

                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
