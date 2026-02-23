package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateKnowledgeArticleRequest;
import com.recipe.manager.dto.request.UpdateKnowledgeArticleRequest;
import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.KnowledgeCategory;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.KnowledgeArticleRepository;
import com.recipe.manager.repository.KnowledgeCategoryRepository;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock
    private KnowledgeCategoryRepository categoryRepository;

    @Mock
    private KnowledgeArticleRepository articleRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private KnowledgeService knowledgeService;

    private User chefUser;
    private User serviceUser;
    private User producerUser;
    private KnowledgeCategory category;

    @BeforeEach
    void setUp() {
        chefUser = User.builder().id(1L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
        serviceUser = User.builder().id(2L).email("service@example.com").name("Service").role(Role.SERVICE).build();
        producerUser = User.builder().id(3L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
        category = KnowledgeCategory.builder().id(1L).name("調理技法").description("調理の工夫").sortOrder(1).build();
    }

    @Test
    void カテゴリ一覧_正常系_ソート順で取得できる() {
        KnowledgeCategory cat2 = KnowledgeCategory.builder().id(2L).name("演出方法").sortOrder(2).build();
        when(categoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(category, cat2));

        List<KnowledgeCategory> result = knowledgeService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("調理技法", result.get(0).getName());
    }

    @Test
    void 記事作成_正常系_全ロールが投稿できる() {
        CreateKnowledgeArticleRequest request = CreateKnowledgeArticleRequest.builder()
                .title("包丁の使い方").content("# 基本\n包丁は...").categoryId(1L).tags("包丁,基本").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(articleRepository.save(any(KnowledgeArticle.class))).thenAnswer(inv -> {
            KnowledgeArticle a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        KnowledgeArticle result = knowledgeService.createArticle(request, chefUser);

        assertEquals("包丁の使い方", result.getTitle());
        assertEquals(chefUser, result.getAuthor());
        verify(articleRepository).save(any(KnowledgeArticle.class));
    }

    @Test
    void 記事作成_異常系_存在しないカテゴリ() {
        CreateKnowledgeArticleRequest request = CreateKnowledgeArticleRequest.builder()
                .title("テスト").content("本文").categoryId(999L).build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> knowledgeService.createArticle(request, chefUser));
    }

    @Test
    void 記事更新_正常系_自分の記事を編集できる() {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("旧タイトル").content("旧本文").category(category).author(chefUser).build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(KnowledgeArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateKnowledgeArticleRequest request = UpdateKnowledgeArticleRequest.builder()
                .title("新タイトル").build();

        KnowledgeArticle result = knowledgeService.updateArticle(1L, request, chefUser);

        assertEquals("新タイトル", result.getTitle());
    }

    @Test
    void 記事更新_正常系_PRODUCERは他人の記事も編集できる() {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("旧タイトル").content("本文").category(category).author(chefUser).build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(KnowledgeArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateKnowledgeArticleRequest request = UpdateKnowledgeArticleRequest.builder()
                .title("プロデューサーが編集").build();

        KnowledgeArticle result = knowledgeService.updateArticle(1L, request, producerUser);

        assertEquals("プロデューサーが編集", result.getTitle());
    }

    @Test
    void 記事更新_異常系_他人の記事は編集できない() {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("タイトル").content("本文").category(category).author(chefUser).build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        assertThrows(ForbiddenException.class,
                () -> knowledgeService.updateArticle(1L,
                        UpdateKnowledgeArticleRequest.builder().title("変更").build(),
                        serviceUser));
    }

    @Test
    void 記事削除_正常系_自分の記事を削除できる() {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("タイトル").content("本文").category(category).author(chefUser).build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        knowledgeService.deleteArticle(1L, chefUser);

        verify(articleRepository).delete(article);
    }

    @Test
    void 記事削除_異常系_他人の記事は削除できない() {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("タイトル").content("本文").category(category).author(chefUser).build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        assertThrows(ForbiddenException.class,
                () -> knowledgeService.deleteArticle(1L, serviceUser));

        verify(articleRepository, never()).delete(any());
    }

    @Test
    void 記事一覧_正常系_カテゴリフィルタ() {
        Pageable pageable = PageRequest.of(0, 20);
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("記事").content("本文").category(category).author(chefUser).build();

        when(articleRepository.findByCategoryId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(article)));

        Page<KnowledgeArticle> result = knowledgeService.listArticles(1L, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void 記事検索_正常系_キーワードで検索できる() {
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(1L).title("包丁技法").content("包丁の使い方").category(category).author(chefUser).build();

        when(articleRepository.searchByKeyword("包丁")).thenReturn(List.of(article));

        List<KnowledgeArticle> result = knowledgeService.searchArticles("包丁");

        assertEquals(1, result.size());
    }
}
