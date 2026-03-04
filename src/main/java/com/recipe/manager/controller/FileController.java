package com.recipe.manager.controller;

import com.recipe.manager.dto.response.FileUploadResponse;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.service.FileUploadService;
import com.recipe.manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;
    private final UserService userService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("target") String target,
            @RequestParam("targetId") Long targetId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        String url = switch (target) {
            case "recipe" -> fileUploadService.uploadForRecipe(targetId, file, currentUser);
            case "cookingStep" -> fileUploadService.uploadForCookingStep(targetId, file, currentUser);
            case "serviceDesign" -> fileUploadService.uploadForServiceDesign(targetId, file, currentUser);
            default -> throw new BusinessLogicException("不正なターゲット: " + target);
        };

        FileUploadResponse response = FileUploadResponse.builder()
                .url(url)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam("target") String target,
            @RequestParam("targetId") Long targetId,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        switch (target) {
            case "recipe" -> fileUploadService.deleteForRecipe(targetId, currentUser);
            case "cookingStep" -> fileUploadService.deleteForCookingStep(targetId, currentUser);
            case "serviceDesign" -> fileUploadService.deleteForServiceDesign(targetId, currentUser);
            default -> throw new BusinessLogicException("不正なターゲット: " + target);
        }

        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
