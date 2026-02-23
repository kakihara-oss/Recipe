package com.recipe.manager.service;

import com.recipe.manager.ai.LlmClient;
import com.recipe.manager.ai.PromptBuilder;
import com.recipe.manager.dto.request.CreateAiThreadRequest;
import com.recipe.manager.entity.AiConsultationMessage;
import com.recipe.manager.entity.AiConsultationThread;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.SenderType;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.AiConsultationMessageRepository;
import com.recipe.manager.repository.AiConsultationThreadRepository;
import com.recipe.manager.repository.KnowledgeArticleRepository;
import com.recipe.manager.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConsultationServiceTest {

    @Mock
    private AiConsultationThreadRepository threadRepository;

    @Mock
    private AiConsultationMessageRepository messageRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Mock
    private LlmClient llmClient;

    @Mock
    private PromptBuilder promptBuilder;

    @InjectMocks
    private AiConsultationService aiConsultationService;

    private User chefUser;
    private User serviceUser;

    @BeforeEach
    void setUp() {
        chefUser = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        serviceUser = User.builder().id(2L).email("service@example.com").name("Service").role(Role.SERVICE).build();
    }

    @Test
    void スレッド作成_正常系_AIの応答が返る() {
        CreateAiThreadRequest request = CreateAiThreadRequest.builder()
                .theme("新メニュー開発").initialMessage("夏向けの冷製パスタを考えたい").build();

        when(threadRepository.save(any(AiConsultationThread.class))).thenAnswer(inv -> {
            AiConsultationThread t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(knowledgeArticleRepository.searchByKeyword(anyString())).thenReturn(List.of());
        when(promptBuilder.buildSystemPrompt(any(), any())).thenReturn("system prompt");
        when(promptBuilder.buildConversationContext(any(), anyString())).thenReturn("user context");
        when(llmClient.chat(anyString(), anyString())).thenReturn("冷製パスタには...");

        AiConsultationThread result = aiConsultationService.createThread(request, chefUser);

        assertEquals("新メニュー開発", result.getTheme());
        assertEquals(2, result.getMessages().size());
        assertEquals(SenderType.USER, result.getMessages().get(0).getSenderType());
        assertEquals(SenderType.AI, result.getMessages().get(1).getSenderType());
    }

    @Test
    void スレッド取得_正常系_自分のスレッドを取得できる() {
        AiConsultationThread thread = AiConsultationThread.builder()
                .id(1L).user(chefUser).theme("テーマ").build();

        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));

        AiConsultationThread result = aiConsultationService.getThread(1L, chefUser);

        assertEquals("テーマ", result.getTheme());
    }

    @Test
    void スレッド取得_異常系_他人のスレッドにアクセスできない() {
        AiConsultationThread thread = AiConsultationThread.builder()
                .id(1L).user(chefUser).theme("テーマ").build();

        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));

        assertThrows(ForbiddenException.class,
                () -> aiConsultationService.getThread(1L, serviceUser));
    }

    @Test
    void スレッド取得_異常系_存在しないスレッド() {
        when(threadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> aiConsultationService.getThread(999L, chefUser));
    }

    @Test
    void 自分のスレッド一覧_正常系() {
        AiConsultationThread thread = AiConsultationThread.builder()
                .id(1L).user(chefUser).theme("テーマ").build();

        when(threadRepository.findByUserIdOrderByUpdatedAtDesc(1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(thread)));

        Page<AiConsultationThread> result =
                aiConsultationService.listMyThreads(chefUser, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void メッセージ送信_正常系_AI応答が保存される() {
        AiConsultationThread thread = AiConsultationThread.builder()
                .id(1L).user(chefUser).theme("テーマ").build();

        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(knowledgeArticleRepository.searchByKeyword(anyString())).thenReturn(List.of());
        when(promptBuilder.buildSystemPrompt(any(), any())).thenReturn("system");
        when(promptBuilder.buildConversationContext(any(), anyString())).thenReturn("context");
        when(llmClient.chat(anyString(), anyString())).thenReturn("AIの回答です");
        when(messageRepository.save(any(AiConsultationMessage.class))).thenAnswer(inv -> {
            AiConsultationMessage m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        AiConsultationMessage result =
                aiConsultationService.sendMessage(1L, "質問です", chefUser);

        assertEquals(SenderType.AI, result.getSenderType());
        assertEquals("AIの回答です", result.getContent());
        verify(llmClient).chat(anyString(), anyString());
    }
}
