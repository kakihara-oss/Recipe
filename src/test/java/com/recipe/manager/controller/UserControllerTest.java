package com.recipe.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.request.UpdateRoleRequest;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static UsernamePasswordAuthenticationToken producerAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "producer@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_PRODUCER")));
    }

    private static UsernamePasswordAuthenticationToken chefAuth() {
        return new UsernamePasswordAuthenticationToken(
                2L, "chef@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_CHEF")));
    }

    private User createTestUser(Long id, String email, String name, Role role) {
        return User.builder()
                .id(id).email(email).name(name).role(role)
                .enabled(true)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    @Test
    void ユーザー一覧取得_正常系_PRODUCERが取得できる() throws Exception {
        List<User> users = List.of(
                createTestUser(1L, "producer@example.com", "Producer", Role.PRODUCER),
                createTestUser(2L, "chef@example.com", "Chef", Role.CHEF)
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                        .with(authentication(producerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("producer@example.com"))
                .andExpect(jsonPath("$[1].role").value("CHEF"));
    }

    @Test
    void ユーザー一覧取得_異常系_CHEF権限では403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(authentication(chefAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void 自分の情報取得_正常系_認証済みユーザー() throws Exception {
        User user = createTestUser(2L, "chef@example.com", "Chef", Role.CHEF);
        when(userService.getUserById(2L)).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(chefAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("chef@example.com"))
                .andExpect(jsonPath("$.role").value("CHEF"));
    }

    @Test
    void 自分の情報取得_異常系_未認証では401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ロール変更_正常系_PRODUCERが変更できる() throws Exception {
        User updated = createTestUser(2L, "chef@example.com", "Chef", Role.SERVICE);
        when(userService.updateRole(eq(2L), eq(Role.SERVICE), eq(1L))).thenReturn(updated);

        UpdateRoleRequest request = new UpdateRoleRequest(Role.SERVICE);

        mockMvc.perform(put("/api/users/2/role")
                        .with(authentication(producerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SERVICE"));
    }

    @Test
    void ロール変更_異常系_CHEF権限では403() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest(Role.SERVICE);

        mockMvc.perform(put("/api/users/2/role")
                        .with(authentication(chefAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
