package com.recipe.manager.security;

import com.recipe.manager.config.JwtProperties;
import com.recipe.manager.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-for-testing-purposes-only-must-be-at-least-256-bits-long-enough");
        properties.setExpirationMs(86400000L);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void トークン生成_正常系_ユーザー情報が取得できる() {
        String token = jwtTokenProvider.createToken(1L, "chef@example.com", Role.CHEF);

        assertEquals(1L, jwtTokenProvider.getUserId(token));
        assertEquals("chef@example.com", jwtTokenProvider.getEmail(token));
        assertEquals(Role.CHEF, jwtTokenProvider.getRole(token));
    }

    @Test
    void トークン検証_正常系_有効なトークン() {
        String token = jwtTokenProvider.createToken(1L, "chef@example.com", Role.CHEF);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void トークン検証_異常系_不正なトークン() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void トークン検証_異常系_期限切れトークン() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret("test-secret-key-for-testing-purposes-only-must-be-at-least-256-bits-long-enough");
        expiredProperties.setExpirationMs(0L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProperties);

        String token = expiredProvider.createToken(1L, "chef@example.com", Role.CHEF);

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void トークン生成_正常系_全ロールでトークンが生成できる() {
        for (Role role : Role.values()) {
            String token = jwtTokenProvider.createToken(1L, "user@example.com", role);
            assertEquals(role, jwtTokenProvider.getRole(token));
        }
    }
}
