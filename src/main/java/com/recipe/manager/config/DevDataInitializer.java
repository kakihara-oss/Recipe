package com.recipe.manager.config;

import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("dev@example.com").isEmpty()) {
            User devUser = User.builder()
                    .googleId("dev-google-id")
                    .email("dev@example.com")
                    .name("開発ユーザー")
                    .role(Role.PRODUCER)
                    .enabled(true)
                    .build();
            userRepository.save(devUser);
            log.info("開発用ユーザーを作成しました: dev@example.com (PRODUCER)");
        }
    }
}
