package com.example.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))  // ✅ enable your CorsConfig
                .csrf(csrf -> csrf.disable())         // ✅ disable CSRF for REST APIs
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()         // ✅ allow all requests without auth
                );
        return http.build();
    }
}