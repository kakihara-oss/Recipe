package com.recipe.manager.controller;

import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.repository.UserRepository;
import com.recipe.manager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Profile("dev")
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> devLogin(@RequestBody Map<String, String> request) {
        String role = request.getOrDefault("role", "PRODUCER");
        Role userRole = Role.valueOf(role);

        String email = "dev-" + role.toLowerCase() + "@example.com";
        String name = "開発ユーザー（" + role + "）";

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .googleId("dev-" + role.toLowerCase())
                    .email(email)
                    .name(name)
                    .role(userRole)
                    .enabled(true)
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());

        log.info("Dev login: {} ({})", user.getName(), user.getRole());

        return ResponseEntity.ok(Map.of("token", token));
    }
}
