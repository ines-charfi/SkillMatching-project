package com.ines.skillmatch_auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class that provides the password encoder bean.
 * This encoder is used throughout the authentication service to
 * securely hash passwords before storing them in the database,
 * and to verify passwords during login.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates a BCryptPasswordEncoder instance as the application's password encoder.
     * BCrypt is a strong, adaptive hashing function that includes a salt
     * and a configurable work factor (strength), making it resistant to
     * brute-force attacks even as hardware improves.
     *
     * @return a BCryptPasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}