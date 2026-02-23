package com.recipe.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret = "default-secret-key-please-change-in-production-environment-this-must-be-at-least-256-bits";

    private long expirationMs = 86400000; // 24 hours
}
