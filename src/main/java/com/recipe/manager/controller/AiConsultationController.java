package com.recipe.manager.controller;

import com.recipe.manager.common.Constants;
import com.recipe.manager.dto.request.CreateAiThreadRequest;
import com.recipe.manager.dto.request.SendAiMessageRequest;
import com.recipe.manager.dto.response.AiMessageResponse;
import com.recipe.manager.dto.response.AiThreadResponse;
import com.recipe.manager.entity.AiConsultationMessage;
import com.recipe.manager.entity.AiConsultationThread;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.AiConsultationService;
import com.recipe.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiConsultationController {

    private final AiConsultationService aiConsultationService;
    private final UserService userService;

    @PostMapping("/threads")
    public ResponseEntity<AiThreadResponse> createThread(
            @Valid @RequestBody CreateAiThreadRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        AiConsultationThread thread = aiConsultationService.createThread(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(AiThreadResponse.from(thread));
    }

    @GetMapping("/threads")
    public ResponseEntity<Page<AiThreadResponse>> listMyThreads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        int pageSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<AiConsultationThread> threads = aiConsultationService.listMyThreads(currentUser, pageable);
        return ResponseEntity.ok(threads.map(AiThreadResponse::from));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<AiThreadResponse> getThread(
            @PathVariable Long threadId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        AiConsultationThread thread = aiConsultationService.getThread(threadId, currentUser);
        return ResponseEntity.ok(AiThreadResponse.from(thread));
    }

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<List<AiMessageResponse>> getMessages(
            @PathVariable Long threadId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<AiMessageResponse> messages = aiConsultationService.getMessages(threadId, currentUser).stream()
                .map(AiMessageResponse::from)
                .toList();
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/threads/{threadId}/messages")
    public ResponseEntity<AiMessageResponse> sendMessage(
            @PathVariable Long threadId,
            @Valid @RequestBody SendAiMessageRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        AiConsultationMessage aiMessage =
                aiConsultationService.sendMessage(threadId, request.getMessage(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(AiMessageResponse.from(aiMessage));
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
