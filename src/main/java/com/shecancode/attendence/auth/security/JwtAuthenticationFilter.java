package com.shecancode.attendence.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("JWT Filter: Processing request to {}", request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");
        log.debug("JWT Filter: Authorization header: {}", authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 20)) + "..." : "null");

        // Pass through if no Bearer token present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("JWT Filter: No Bearer token found, passing through");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            log.debug("Processing JWT token: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
            final String username = jwtService.extractUsername(jwt);
            log.debug("Extracted username from JWT: {}", username);

            // Only authenticate if not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("Loaded user details for {}: {}", username, userDetails.getAuthorities());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Successfully authenticated user [{}] with roles {}", username, userDetails.getAuthorities());
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                }
            }
        } catch (Exception ex) {
            log.error("JWT validation failed with exception: {}", ex.getMessage(), ex);
            // Don't set authentication — Spring Security will return 401 automatically
        }

        filterChain.doFilter(request, response);
    }
}
