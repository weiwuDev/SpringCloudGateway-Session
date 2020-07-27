package com.weiwudev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ServerSecurityContextRepository securityContextRepository = new WebSessionServerSecurityContextRepository();

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers("/AuthService/login", "/checkno", "/AuthService/logout", "/RegistrationService/register", "/RegistrationService/check", "/AuthService/check").permitAll()
                .pathMatchers("/check").authenticated()
                .anyExchange().authenticated()
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .cors().and()
                .build();
    }

}
