package com.recipe.manager.ai;

import com.recipe.manager.entity.AiConsultationMessage;
import com.recipe.manager.entity.AiConsultationThread;
import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.Recipe;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    private static final String SYSTEM_PROMPT_BASE = """
            あなたは感動創出レストランの専門AIアドバイザーです。
            料理とサービスを通じてお客様の人生に感動を届けることを使命としています。

            以下の観点からアドバイスを提供してください：
            - 調理技法と食材の活かし方
            - サービスと演出の方法
            - お客様の体験設計
            - 料理の歴史や文化的背景

            回答はMarkdown形式で、具体的かつ実践的な内容にしてください。
            """;

    public String buildSystemPrompt(AiConsultationThread thread,
                                     List<KnowledgeArticle> relatedArticles) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT_BASE);

        sb.append("\n## 相談テーマ\n").append(thread.getTheme()).append("\n");

        if (thread.getRecipe() != null) {
            Recipe recipe = thread.getRecipe();
            sb.append("\n## 関連レシピ\n");
            sb.append("- タイトル: ").append(recipe.getTitle()).append("\n");
            if (recipe.getDescription() != null) {
                sb.append("- 説明: ").append(recipe.getDescription()).append("\n");
            }
            if (recipe.getConcept() != null) {
                sb.append("- コンセプト: ").append(recipe.getConcept()).append("\n");
            }
        }

        if (relatedArticles != null && !relatedArticles.isEmpty()) {
            sb.append("\n## 参考ナレッジ\n");
            for (KnowledgeArticle article : relatedArticles) {
                sb.append("### ").append(article.getTitle()).append("\n");
                sb.append("カテゴリ: ").append(article.getCategory().getName()).append("\n");
                sb.append(article.getContent()).append("\n\n");
            }
        }

        return sb.toString();
    }

    public String buildConversationContext(List<AiConsultationMessage> previousMessages,
                                           String newUserMessage) {
        StringBuilder sb = new StringBuilder();

        if (previousMessages != null && !previousMessages.isEmpty()) {
            sb.append("## これまでの会話\n");
            for (AiConsultationMessage msg : previousMessages) {
                String role = msg.getSenderType().name().equals("USER") ? "ユーザー" : "AI";
                sb.append(role).append(": ").append(msg.getContent()).append("\n\n");
            }
        }

        sb.append("## 新しい質問\n").append(newUserMessage);
        return sb.toString();
    }
}
