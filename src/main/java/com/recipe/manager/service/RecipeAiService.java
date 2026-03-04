package com.recipe.manager.service;

import com.recipe.manager.ai.AiResponseParser;
import com.recipe.manager.ai.LlmClient;
import com.recipe.manager.ai.RecipeAiPromptBuilder;
import com.recipe.manager.dto.request.AiImproveFieldRequest;
import com.recipe.manager.dto.request.AiModifyRecipeRequest;
import com.recipe.manager.dto.response.AiImproveFieldResponse;
import com.recipe.manager.dto.response.AiRecipeDraftResponse;
import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.KnowledgeArticleRepository;
import com.recipe.manager.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeAiService {

    private final LlmClient llmClient;
    private final RecipeAiPromptBuilder promptBuilder;
    private final AiResponseParser responseParser;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final RecipeRepository recipeRepository;

    public AiRecipeDraftResponse generateFromTheme(String theme) {
        log.info("AI recipe generation from theme: {}", theme);

        List<KnowledgeArticle> articles = findRelatedArticles(theme);
        String systemPrompt = promptBuilder.buildGenerateSystemPrompt(articles);
        String userMessage = "テーマ: " + theme;

        String response = llmClient.chat(systemPrompt, userMessage);
        return responseParser.parse(response, AiRecipeDraftResponse.class);
    }

    @Transactional(readOnly = true)
    public AiRecipeDraftResponse generateFromRecipe(Long recipeId, String arrangementInstruction) {
        log.info("AI recipe generation from existing recipe: id={}", recipeId);

        Recipe recipe = recipeRepository.findByIdAndStatusNot(recipeId, RecipeStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        List<KnowledgeArticle> articles = findRelatedArticles(recipe.getTitle());
        String systemPrompt = promptBuilder.buildGenerateFromRecipeSystemPrompt(recipe, articles);

        String userMessage = "このレシピをベースに新しいアレンジ版を提案してください。";
        if (arrangementInstruction != null && !arrangementInstruction.isBlank()) {
            userMessage += "\nアレンジの方向性: " + arrangementInstruction;
        }

        String response = llmClient.chat(systemPrompt, userMessage);
        return responseParser.parse(response, AiRecipeDraftResponse.class);
    }

    public AiImproveFieldResponse improveField(AiImproveFieldRequest request) {
        log.info("AI field improvement: field={}", request.getFieldName());

        List<KnowledgeArticle> articles = findRelatedArticles(
                request.getRecipeContext() != null ? request.getRecipeContext() : request.getFieldName());
        String systemPrompt = promptBuilder.buildImproveFieldSystemPrompt(articles);

        String userMessage = String.format(
                "フィールド名: %s\n現在の値: %s\nレシピの文脈: %s",
                request.getFieldName(),
                request.getCurrentValue() != null ? request.getCurrentValue() : "(未入力)",
                request.getRecipeContext() != null ? request.getRecipeContext() : "(なし)");

        String response = llmClient.chat(systemPrompt, userMessage);
        return responseParser.parse(response, AiImproveFieldResponse.class);
    }

    public AiRecipeDraftResponse modifyRecipe(AiModifyRecipeRequest request) {
        log.info("AI recipe modification: instruction={}", request.getInstruction());

        List<KnowledgeArticle> articles = findRelatedArticles(request.getInstruction());
        String systemPrompt = promptBuilder.buildModifySystemPrompt(articles);

        StringBuilder userMessage = new StringBuilder();
        userMessage.append("## 修正指示\n").append(request.getInstruction()).append("\n\n");

        if (request.getCurrentRecipe() != null) {
            AiModifyRecipeRequest.CurrentRecipe cr = request.getCurrentRecipe();
            userMessage.append("## 現在のレシピ\n");
            if (cr.getTitle() != null) userMessage.append("タイトル: ").append(cr.getTitle()).append("\n");
            if (cr.getDescription() != null) userMessage.append("説明: ").append(cr.getDescription()).append("\n");
            if (cr.getCategory() != null) userMessage.append("カテゴリ: ").append(cr.getCategory()).append("\n");
            if (cr.getServings() != null) userMessage.append("人数: ").append(cr.getServings()).append("人前\n");
            if (cr.getConcept() != null) userMessage.append("コンセプト: ").append(cr.getConcept()).append("\n");
            if (cr.getStory() != null) userMessage.append("ストーリー: ").append(cr.getStory()).append("\n");

            if (cr.getCookingSteps() != null && !cr.getCookingSteps().isEmpty()) {
                userMessage.append("\n手順:\n");
                for (AiModifyRecipeRequest.StepData step : cr.getCookingSteps()) {
                    userMessage.append(step.getStepNumber()).append(". ").append(step.getDescription()).append("\n");
                }
            }
        }

        String response = llmClient.chat(systemPrompt, userMessage.toString());
        return responseParser.parse(response, AiRecipeDraftResponse.class);
    }

    private List<KnowledgeArticle> findRelatedArticles(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String searchKey = keyword.length() > 50 ? keyword.substring(0, 50) : keyword;
        List<KnowledgeArticle> articles = knowledgeArticleRepository.searchByKeyword(searchKey);
        if (articles.size() > 5) {
            return articles.subList(0, 5);
        }
        return articles;
    }
}
