package com.recipe.manager.service;

import com.recipe.manager.config.AppProperties;
import com.recipe.manager.entity.CookingStep;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.ServiceDesign;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.CookingStepRepository;
import com.recipe.manager.repository.RecipeRepository;
import com.recipe.manager.repository.ServiceDesignRepository;
import com.recipe.manager.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final FileStorageService fileStorageService;
    private final RecipeRepository recipeRepository;
    private final CookingStepRepository cookingStepRepository;
    private final ServiceDesignRepository serviceDesignRepository;
    private final AppProperties appProperties;

    @Transactional
    public String uploadForRecipe(Long recipeId, MultipartFile file, User currentUser) {
        validateEditPermission(currentUser);
        validateFile(file);

        Recipe recipe = findRecipe(recipeId);
        String oldUrl = recipe.getImageUrl();

        String newUrl = fileStorageService.store(file);
        recipe.setImageUrl(newUrl);
        recipeRepository.save(recipe);

        deleteOldFile(oldUrl);
        log.info("Recipe image uploaded: recipeId={}, url={}", recipeId, newUrl);
        return newUrl;
    }

    @Transactional
    public String uploadForCookingStep(Long stepId, MultipartFile file, User currentUser) {
        validateEditPermission(currentUser);
        validateFile(file);

        CookingStep step = cookingStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("CookingStep", stepId));
        String oldUrl = step.getImageUrl();

        String newUrl = fileStorageService.store(file);
        step.setImageUrl(newUrl);
        cookingStepRepository.save(step);

        deleteOldFile(oldUrl);
        log.info("CookingStep image uploaded: stepId={}, url={}", stepId, newUrl);
        return newUrl;
    }

    @Transactional
    public String uploadForServiceDesign(Long serviceDesignId, MultipartFile file, User currentUser) {
        validateServiceDesignPermission(currentUser);
        validateFile(file);

        ServiceDesign sd = serviceDesignRepository.findById(serviceDesignId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceDesign", serviceDesignId));
        String oldUrl = sd.getPlatingImageUrl();

        String newUrl = fileStorageService.store(file);
        sd.setPlatingImageUrl(newUrl);
        serviceDesignRepository.save(sd);

        deleteOldFile(oldUrl);
        log.info("ServiceDesign plating image uploaded: id={}, url={}", serviceDesignId, newUrl);
        return newUrl;
    }

    @Transactional
    public void deleteForRecipe(Long recipeId, User currentUser) {
        validateEditPermission(currentUser);
        Recipe recipe = findRecipe(recipeId);
        String oldUrl = recipe.getImageUrl();
        recipe.setImageUrl(null);
        recipeRepository.save(recipe);
        deleteOldFile(oldUrl);
    }

    @Transactional
    public void deleteForCookingStep(Long stepId, User currentUser) {
        validateEditPermission(currentUser);
        CookingStep step = cookingStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("CookingStep", stepId));
        String oldUrl = step.getImageUrl();
        step.setImageUrl(null);
        cookingStepRepository.save(step);
        deleteOldFile(oldUrl);
    }

    @Transactional
    public void deleteForServiceDesign(Long serviceDesignId, User currentUser) {
        validateServiceDesignPermission(currentUser);
        ServiceDesign sd = serviceDesignRepository.findById(serviceDesignId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceDesign", serviceDesignId));
        String oldUrl = sd.getPlatingImageUrl();
        sd.setPlatingImageUrl(null);
        serviceDesignRepository.save(sd);
        deleteOldFile(oldUrl);
    }

    private Recipe findRecipe(Long recipeId) {
        return recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessLogicException("ファイルが空です");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessLogicException("ファイルサイズが上限（10MB）を超えています");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessLogicException("許可されていないファイル形式です。JPEG, PNG, WebPのみ対応しています");
        }
    }

    private void validateEditPermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.PRODUCER) {
            throw new ForbiddenException("レシピの編集権限がありません");
        }
    }

    private void validateServiceDesignPermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.SERVICE && role != Role.PRODUCER) {
            throw new ForbiddenException("サービス設計の編集権限がありません");
        }
    }

    private void deleteOldFile(String oldUrl) {
        if (oldUrl != null && !oldUrl.isBlank()) {
            fileStorageService.delete(oldUrl);
        }
    }
}
