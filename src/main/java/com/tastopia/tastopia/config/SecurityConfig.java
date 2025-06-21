package com.tastopia.tastopia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll() // Allow Swagger access
                .anyRequest().authenticated() // Secure all other endpoints
            )
            .httpBasic() // Use Basic Auth
            .and()
            .csrf().disable(); // Disable CSRF for simplicity
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.withUsername("admin")
            .password("{noop}password") // {noop} for plain-text (for learning; use BCrypt in production)
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}