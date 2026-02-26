package com.recipe.manager.config;

import com.recipe.manager.common.Constants;
import com.recipe.manager.security.JwtAuthenticationFilter;
import com.recipe.manager.security.JwtTokenProvider;
import com.recipe.manager.security.OAuth2AuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private JwtTokenProvider jwtTokenProvider;

    @Autowired(required = false)
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        if (isDev) {
            log.info("Dev profile detected: configuring security to permit all requests");
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                            UsernamePasswordAuthenticationFilter.class);
        } else {
            log.info("Production profile: configuring OAuth2 + JWT security");
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/health").permitAll()
                            .requestMatchers("/oauth2/**", "/login/**").permitAll()
                            .requestMatchers(HttpMethod.PUT, "/api/users/*/role")
                                .hasRole(Constants.ROLE_PRODUCER)
                            .requestMatchers(HttpMethod.GET, "/api/users")
                                .hasRole(Constants.ROLE_PRODUCER)
                            .anyRequest().authenticated()
                    )
                    .oauth2Login(oauth2 -> oauth2
                            .successHandler(oAuth2AuthenticationSuccessHandler)
                    )
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                            UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
