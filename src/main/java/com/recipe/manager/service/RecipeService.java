package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateRecipeRequest;
import com.recipe.manager.dto.request.UpdateExperienceDesignRequest;
import com.recipe.manager.dto.request.UpdateRecipeRequest;
import com.recipe.manager.dto.request.UpdateServiceDesignRequest;
import com.recipe.manager.entity.CookingStep;
import com.recipe.manager.entity.ExperienceDesign;
import com.recipe.manager.entity.Ingredient;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeHistory;
import com.recipe.manager.entity.RecipeIngredient;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.ServiceDesign;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.IngredientRepository;
import com.recipe.manager.repository.RecipeHistoryRepository;
import com.recipe.manager.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeHistoryRepository recipeHistoryRepository;

    @Transactional
    public Recipe createRecipe(CreateRecipeRequest request, User currentUser) {
        validateRecipeCreatePermission(currentUser);

        Recipe recipe = Recipe.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .servings(request.getServings())
                .concept(request.getConcept())
                .story(request.getStory())
                .createdBy(currentUser)
                .build();

        if (request.getCookingSteps() != null) {
            for (CreateRecipeRequest.CookingStepInput stepInput : request.getCookingSteps()) {
                CookingStep step = CookingStep.builder()
                        .recipe(recipe)
                        .stepNumber(stepInput.getStepNumber())
                        .description(stepInput.getDescription())
                        .durationMinutes(stepInput.getDurationMinutes())
                        .temperature(stepInput.getTemperature())
                        .tips(stepInput.getTips())
                        .build();
                recipe.getCookingSteps().add(step);
            }
        }

        if (request.getIngredients() != null) {
            int sortOrder = 0;
            for (CreateRecipeRequest.IngredientInput ingredientInput : request.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(ingredientInput.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Ingredient", ingredientInput.getIngredientId()));

                RecipeIngredient ri = RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .quantity(ingredientInput.getQuantity())
                        .unit(ingredientInput.getUnit())
                        .preparationNote(ingredientInput.getPreparationNote())
                        .substitutes(ingredientInput.getSubstitutes())
                        .sortOrder(sortOrder++)
                        .build();
                recipe.getIngredients().add(ri);
            }
        }

        Recipe saved = recipeRepository.save(recipe);
        recordHistory(saved, currentUser, "CREATE", "レシピ新規作成");

        log.info("Recipe created: id={}, title={}, by={}",
                saved.getId(), saved.getTitle(), currentUser.getEmail());

        return saved;
    }

    @Transactional(readOnly = true)
    public Recipe getRecipeById(Long id) {
        return recipeRepository.findByIdAndStatusNot(id, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));
    }

    @Transactional(readOnly = true)
    public Page<Recipe> listRecipes(String category, RecipeStatus status, Pageable pageable) {
        if (status != null) {
            if (status == RecipeStatus.DELETED) {
                throw new BusinessLogicException("削除済みレシピは一覧で取得できません");
            }
            return recipeRepository.findByStatus(status, pageable);
        }
        if (category != null) {
            return recipeRepository.findByStatusNotAndCategory(
                    RecipeStatus.DELETED, category, pageable);
        }
        return recipeRepository.findByStatusNot(RecipeStatus.DELETED, pageable);
    }

    @Transactional
    public Recipe updateRecipe(Long id, UpdateRecipeRequest request, User currentUser) {
        validateRecipeEditPermission(currentUser);

        Recipe recipe = getRecipeById(id);
        StringJoiner changedFields = new StringJoiner(", ");

        if (request.getTitle() != null) {
            changedFields.add("title");
            recipe.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            changedFields.add("description");
            recipe.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            changedFields.add("category");
            recipe.setCategory(request.getCategory());
        }
        if (request.getServings() != null) {
            changedFields.add("servings");
            recipe.setServings(request.getServings());
        }
        if (request.getConcept() != null) {
            changedFields.add("concept");
            recipe.setConcept(request.getConcept());
        }
        if (request.getStory() != null) {
            changedFields.add("story");
            recipe.setStory(request.getStory());
        }

        Recipe saved = recipeRepository.save(recipe);
        recordHistory(saved, currentUser, "UPDATE", changedFields.toString());

        log.info("Recipe updated: id={}, fields={}, by={}",
                id, changedFields, currentUser.getEmail());

        return saved;
    }

    @Transactional
    public Recipe updateServiceDesign(Long id, UpdateServiceDesignRequest request, User currentUser) {
        validateServiceDesignPermission(currentUser);

        Recipe recipe = getRecipeById(id);

        ServiceDesign serviceDesign = recipe.getServiceDesign();
        if (serviceDesign == null) {
            serviceDesign = ServiceDesign.builder()
                    .recipe(recipe)
                    .build();
            recipe.setServiceDesign(serviceDesign);
        }

        serviceDesign.setPlatingInstructions(request.getPlatingInstructions());
        serviceDesign.setServiceMethod(request.getServiceMethod());
        serviceDesign.setCustomerScript(request.getCustomerScript());
        serviceDesign.setStagingMethod(request.getStagingMethod());
        serviceDesign.setTiming(request.getTiming());
        serviceDesign.setStorytelling(request.getStorytelling());

        Recipe saved = recipeRepository.save(recipe);
        recordHistory(saved, currentUser, "UPDATE_SERVICE_DESIGN", "serviceDesign");

        log.info("ServiceDesign updated: recipeId={}, by={}",
                id, currentUser.getEmail());

        return saved;
    }

    @Transactional
    public Recipe updateExperienceDesign(Long id, UpdateExperienceDesignRequest request, User currentUser) {
        validateServiceDesignPermission(currentUser);

        Recipe recipe = getRecipeById(id);

        ExperienceDesign experienceDesign = recipe.getExperienceDesign();
        if (experienceDesign == null) {
            experienceDesign = ExperienceDesign.builder()
                    .recipe(recipe)
                    .build();
            recipe.setExperienceDesign(experienceDesign);
        }

        experienceDesign.setTargetScene(request.getTargetScene());
        experienceDesign.setEmotionalKeyPoints(request.getEmotionalKeyPoints());
        experienceDesign.setSpecialOccasionSupport(request.getSpecialOccasionSupport());
        experienceDesign.setSeasonalPresentation(request.getSeasonalPresentation());
        experienceDesign.setSensoryAppeal(request.getSensoryAppeal());

        Recipe saved = recipeRepository.save(recipe);
        recordHistory(saved, currentUser, "UPDATE_EXPERIENCE_DESIGN", "experienceDesign");

        log.info("ExperienceDesign updated: recipeId={}, by={}",
                id, currentUser.getEmail());

        return saved;
    }

    @Transactional
    public Recipe updateStatus(Long id, RecipeStatus newStatus, User currentUser) {
        validateRecipeEditPermission(currentUser);

        Recipe recipe = getRecipeById(id);
        RecipeStatus oldStatus = recipe.getStatus();

        validateStatusTransition(oldStatus, newStatus);

        recipe.setStatus(newStatus);
        Recipe saved = recipeRepository.save(recipe);

        recordHistory(saved, currentUser, "STATUS_CHANGE",
                oldStatus.name() + " -> " + newStatus.name());

        log.info("Recipe status changed: id={}, {} -> {}, by={}",
                id, oldStatus, newStatus, currentUser.getEmail());

        return saved;
    }

    @Transactional
    public void deleteRecipe(Long id, User currentUser) {
        validateRecipeEditPermission(currentUser);

        Recipe recipe = getRecipeById(id);
        recipe.setStatus(RecipeStatus.DELETED);
        recipeRepository.save(recipe);

        recordHistory(recipe, currentUser, "DELETE", "論理削除");

        log.info("Recipe deleted (logical): id={}, by={}",
                id, currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<RecipeHistory> getRecipeHistory(Long recipeId) {
        getRecipeById(recipeId);
        return recipeHistoryRepository.findByRecipeIdOrderByChangedAtDesc(recipeId);
    }

    private void validateRecipeCreatePermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.PRODUCER) {
            throw new ForbiddenException("レシピの作成権限がありません");
        }
    }

    private void validateRecipeEditPermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.PRODUCER) {
            throw new ForbiddenException("レシピの編集権限がありません");
        }
    }

    private void validateServiceDesignPermission(User user) {
        Role role = user.getRole();
        if (role != Role.CHEF && role != Role.SERVICE && role != Role.PRODUCER) {
            throw new ForbiddenException("サービス・体験設計の編集権限がありません");
        }
    }

    private void validateStatusTransition(RecipeStatus from, RecipeStatus to) {
        if (to == RecipeStatus.DELETED) {
            throw new BusinessLogicException("ステータスをDELETEDに変更することはできません。削除APIを使用してください");
        }

        List<RecipeStatus> allowedTransitions = switch (from) {
            case DRAFT -> List.of(RecipeStatus.PUBLISHED);
            case PUBLISHED -> List.of(RecipeStatus.ARCHIVED);
            case ARCHIVED -> List.of(RecipeStatus.PUBLISHED);
            case DELETED -> new ArrayList<>();
        };

        if (!allowedTransitions.contains(to)) {
            throw new BusinessLogicException(
                    String.format("ステータスを%sから%sに変更することはできません", from.name(), to.name()));
        }
    }

    private void recordHistory(Recipe recipe, User user, String changeType, String changedFields) {
        RecipeHistory history = RecipeHistory.builder()
                .recipe(recipe)
                .changedBy(user)
                .changeType(changeType)
                .changedFields(changedFields)
                .build();
        recipeHistoryRepository.save(history);
    }
}
