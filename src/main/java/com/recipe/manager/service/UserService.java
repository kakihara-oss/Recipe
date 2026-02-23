package com.recipe.manager.service;

import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User updateRole(Long userId, Role newRole, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new BusinessLogicException("自分自身のロールは変更できません");
        }

        User user = getUserById(userId);
        Role oldRole = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);

        log.info("User role updated: {} ({} -> {}), changed by userId: {}",
                user.getEmail(), oldRole, newRole, currentUserId);

        return saved;
    }
}
