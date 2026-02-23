package com.recipe.manager.service;

import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void ユーザー一覧取得_正常系_有効ユーザーのみ返す() {
        User user1 = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).enabled(true).build();
        User user2 = User.builder().id(2L).email("service@example.com").name("Service").role(Role.SERVICE).enabled(true).build();
        when(userRepository.findByEnabledTrue()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findByEnabledTrue();
    }

    @Test
    void ユーザー取得_正常系_IDで取得できる() {
        User user = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals("chef@example.com", result.getEmail());
    }

    @Test
    void ユーザー取得_異常系_存在しないID() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void ロール変更_正常系_PRODUCERが他ユーザーのロールを変更できる() {
        User targetUser = User.builder().id(2L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateRole(2L, Role.SERVICE, 1L);

        assertEquals(Role.SERVICE, result.getRole());
        verify(userRepository).save(targetUser);
    }

    @Test
    void ロール変更_異常系_自分自身のロールは変更できない() {
        assertThrows(BusinessLogicException.class,
                () -> userService.updateRole(1L, Role.SERVICE, 1L));

        verify(userRepository, never()).save(any());
    }

    @Test
    void ロール変更_異常系_存在しないユーザー() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateRole(999L, Role.SERVICE, 1L));
    }

    @Test
    void メール検索_正常系_メールで取得できる() {
        User user = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        when(userRepository.findByEmail("chef@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("chef@example.com");

        assertEquals(1L, result.getId());
    }

    @Test
    void メール検索_異常系_存在しないメール() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("unknown@example.com"));
    }
}
