package com.historyTalk.config;

import com.historyTalk.security.JwtAuthenticationFilter;
import com.historyTalk.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.Collections;

/**
 * ⚠️ SHARED FILE - Team Coordination Required
 * 
 * This file manages API security for ALL modules.
 * When adding new modules, coordinate before modifying this file.
 * 
 * ENDPOINT CONVENTION:
 * - /v1/historical-contexts/** → Historical Context Module
 * - /api/v1/user/** → User Module (future)
 * - /api/v1/auth/** → Auth Module (future)
 * - /swagger-ui/** → Public (Swagger)
 * - /v3/api-docs/** → Public (OpenAPI spec)
 * 
 * BEFORE MODIFYING:
 * 1. Check git log to see who last modified this
 * 2. Add your endpoint pattern in authorizeHttpRequests()
 * 3. Document which module uses it
 * 4. Build & test: mvn clean install
 * 5. Create Pull Request with description
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI and API Docs - public access
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        
                        // Auth endpoints - public access
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        
                        // Historical Context endpoints - GET requests public, others require authentication
                        .requestMatchers(HttpMethod.GET, "/v1/historical-contexts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/historical-contexts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/v1/historical-contexts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/v1/historical-contexts/**").authenticated()
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
