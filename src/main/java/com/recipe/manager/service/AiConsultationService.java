package com.recipe.manager.service;

import com.recipe.manager.ai.LlmClient;
import com.recipe.manager.ai.PromptBuilder;
import com.recipe.manager.dto.request.CreateAiThreadRequest;
import com.recipe.manager.entity.AiConsultationMessage;
import com.recipe.manager.entity.AiConsultationThread;
import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.Recipe;
import com.recipe.manager.entity.RecipeStatus;
import com.recipe.manager.entity.SenderType;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.AiConsultationMessageRepository;
import com.recipe.manager.repository.AiConsultationThreadRepository;
import com.recipe.manager.repository.KnowledgeArticleRepository;
import com.recipe.manager.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConsultationService {

    private final AiConsultationThreadRepository threadRepository;
    private final AiConsultationMessageRepository messageRepository;
    private final RecipeRepository recipeRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final LlmClient llmClient;
    private final PromptBuilder promptBuilder;

    @Transactional
    public AiConsultationThread createThread(CreateAiThreadRequest request, User currentUser) {
        AiConsultationThread thread = AiConsultationThread.builder()
                .user(currentUser)
                .theme(request.getTheme())
                .build();

        if (request.getRecipeId() != null) {
            Recipe recipe = recipeRepository.findByIdAndStatusNot(request.getRecipeId(), RecipeStatus.DELETED)
                    .orElseThrow(() -> new ResourceNotFoundException("Recipe", request.getRecipeId()));
            thread.setRecipe(recipe);
        }

        AiConsultationThread saved = threadRepository.save(thread);

        AiConsultationMessage userMessage = AiConsultationMessage.builder()
                .thread(saved)
                .senderType(SenderType.USER)
                .content(request.getInitialMessage())
                .build();
        saved.getMessages().add(userMessage);

        List<KnowledgeArticle> relatedArticles = findRelatedArticles(request.getTheme(), request.getRecipeId());
        String systemPrompt = promptBuilder.buildSystemPrompt(saved, relatedArticles);
        String conversationContext = promptBuilder.buildConversationContext(List.of(), request.getInitialMessage());

        String aiResponse = llmClient.chat(systemPrompt, conversationContext);

        AiConsultationMessage aiMessage = AiConsultationMessage.builder()
                .thread(saved)
                .senderType(SenderType.AI)
                .content(aiResponse)
                .referencedArticles(relatedArticles)
                .build();
        saved.getMessages().add(aiMessage);

        threadRepository.save(saved);

        log.info("AI consultation thread created: id={}, theme={}, by={}",
                saved.getId(), saved.getTheme(), currentUser.getEmail());

        return saved;
    }

    @Transactional(readOnly = true)
    public AiConsultationThread getThread(Long threadId, User currentUser) {
        AiConsultationThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("AiConsultationThread", threadId));
        validateThreadAccess(thread, currentUser);
        return thread;
    }

    @Transactional(readOnly = true)
    public Page<AiConsultationThread> listMyThreads(User currentUser, Pageable pageable) {
        return threadRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public List<AiConsultationMessage> getMessages(Long threadId, User currentUser) {
        AiConsultationThread thread = getThread(threadId, currentUser);
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId());
    }

    @Transactional
    public AiConsultationMessage sendMessage(Long threadId, String userMessageContent, User currentUser) {
        AiConsultationThread thread = getThread(threadId, currentUser);

        AiConsultationMessage userMessage = AiConsultationMessage.builder()
                .thread(thread)
                .senderType(SenderType.USER)
                .content(userMessageContent)
                .build();
        messageRepository.save(userMessage);

        List<AiConsultationMessage> previousMessages =
                messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);

        List<KnowledgeArticle> relatedArticles =
                findRelatedArticles(userMessageContent, thread.getRecipe() != null ? thread.getRecipe().getId() : null);

        String systemPrompt = promptBuilder.buildSystemPrompt(thread, relatedArticles);
        String conversationContext = promptBuilder.buildConversationContext(previousMessages, userMessageContent);

        String aiResponse = llmClient.chat(systemPrompt, conversationContext);

        AiConsultationMessage aiMessage = AiConsultationMessage.builder()
                .thread(thread)
                .senderType(SenderType.AI)
                .content(aiResponse)
                .referencedArticles(relatedArticles)
                .build();
        AiConsultationMessage savedAiMessage = messageRepository.save(aiMessage);

        log.info("AI message sent: threadId={}, by={}", threadId, currentUser.getEmail());

        return savedAiMessage;
    }

    private void validateThreadAccess(AiConsultationThread thread, User currentUser) {
        if (!thread.getUser().getId().equals(currentUser.getId())
                && currentUser.getRole() != com.recipe.manager.entity.Role.PRODUCER) {
            throw new ForbiddenException("他のユーザーの相談スレッドにアクセスする権限がありません");
        }
    }

    private List<KnowledgeArticle> findRelatedArticles(String keyword, Long recipeId) {
        List<KnowledgeArticle> articles = knowledgeArticleRepository.searchByKeyword(keyword);

        if (recipeId != null) {
            List<KnowledgeArticle> recipeArticles =
                    knowledgeArticleRepository.findByRelatedRecipeId(recipeId);
            for (KnowledgeArticle a : recipeArticles) {
                if (articles.stream().noneMatch(existing -> existing.getId().equals(a.getId()))) {
                    articles.add(a);
                }
            }
        }

        return articles;
    }
}
