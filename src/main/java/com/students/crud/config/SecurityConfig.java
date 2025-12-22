package com.students.crud.config;

import com.students.crud.Handler.CustomAccessDeniedHandler;
import com.students.crud.security.JwtAuthenticationFilter;
import com.students.crud.security.BasicAuthenticationFilter;
import com.students.crud.security.ApiKeyAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Enhanced Security Configuration
 *
 * Supports multiple authentication methods:
 * 1. JWT Authentication - via JwtAuthenticationFilter
 * 2. Basic Authentication - via BasicAuthenticationFilter
 * 3. API Key Authentication - via ApiKeyAuthenticationFilter
 * 4. Multi-Factor Authentication - via MFA endpoints in AuthenticationController
 * 5. Session-based Form Login - enabled (for traditional web flows)
 * 6. OAuth2 Login (Google/GitHub) - enabled when client registrations exist
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private BasicAuthenticationFilter basicAuthenticationFilter;

    @Autowired
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    // Optional provider - may be missing if no oauth2 clients configured
    @Autowired
    private ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Use IF_REQUIRED session policy so that JWT/API Key/Basic remain stateless but
        // session-based flows (form login, OAuth2) can create and use HTTP session when needed.
        http
                .csrf(csrf -> csrf.disable()) // Note: For production enable CSRF for browser-based flows
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - accessible without authentication
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/auth/health").permitAll()
                        .requestMatchers("/api/auth/methods").permitAll()

                        // Role-based access control
                        .requestMatchers("/api/students/simple/**").hasRole("JUNIOR")
                        .requestMatchers("/api/students/**").hasRole("SENIOR")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))

                // Add authentication filters in specific order
                // Order matters: check API Key first, then Basic Auth, then JWT
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(basicAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Enable form login for session-based authentication (traditional web apps)
                .formLogin(form -> form
                        .permitAll()
                );

        // Enable OAuth2 login only when client registrations are configured
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login();
        }

        return http.build();
    }
}
