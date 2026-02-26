package com.recipe.manager.controller;

import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.UserRepository;
import com.recipe.manager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@Profile("dev")
@RequiredArgsConstructor
public class DevController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getDevToken() {
        User user = userRepository.findByEmail("dev@example.com")
                .orElseThrow(() -> new ResourceNotFoundException("開発用ユーザーが見つかりません"));

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "usage", "curl -H 'Authorization: Bearer <token>' http://localhost:8080/api/recipes"
        ));
    }
}
