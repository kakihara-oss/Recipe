package com.recipe.manager.ai;

import com.recipe.manager.entity.KnowledgeArticle;
import com.recipe.manager.entity.Recipe;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecipeAiPromptBuilder {

    private static final String JSON_RECIPE_SCHEMA = """
            {
              "title": "レシピタイトル",
              "description": "レシピの説明",
              "category": "カテゴリ名",
              "servings": 4,
              "concept": "コンセプト",
              "story": "ストーリー",
              "cookingSteps": [
                {
                  "stepNumber": 1,
                  "description": "手順の説明",
                  "durationMinutes": 10,
                  "temperature": "180℃",
                  "tips": "コツ"
                }
              ],
              "ingredients": [
                {
                  "name": "食材名",
                  "quantity": "100g",
                  "preparationNote": "下処理メモ"
                }
              ]
            }
            """;

    private static final String JSON_FIELD_SCHEMA = """
            {
              "fieldName": "フィールド名",
              "improvedValue": "改善されたテキスト",
              "explanation": "改善理由の説明"
            }
            """;

    public String buildGenerateSystemPrompt(List<KnowledgeArticle> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                あなたは感動創出レストランの専門AIアシスタントです。
                ユーザーが指定したテーマに基づいて、レシピの下書きを生成してください。

                料理とサービスを通じてお客様の人生に感動を届けることを使命としています。
                以下の観点を含めてください：
                - 調理技法と食材の活かし方
                - お客様の体験設計（コンセプト、ストーリー）
                - 具体的な調理手順

                必ず以下のJSON形式で回答してください。JSON以外のテキストは含めないでください。

                """);
        sb.append("スキーマ:\n```json\n").append(JSON_RECIPE_SCHEMA).append("```\n\n");
        appendKnowledgeContext(sb, articles);
        return sb.toString();
    }

    public String buildGenerateFromRecipeSystemPrompt(Recipe recipe, List<KnowledgeArticle> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                あなたは感動創出レストランの専門AIアシスタントです。
                既存のレシピを参考に、新しいアレンジ版レシピを提案してください。

                元のレシピの良さを活かしつつ、新しい視点や季節感、
                お客様への感動ポイントを加えてください。

                必ず以下のJSON形式で回答してください。JSON以外のテキストは含めないでください。

                """);
        sb.append("スキーマ:\n```json\n").append(JSON_RECIPE_SCHEMA).append("```\n\n");

        sb.append("## 元のレシピ\n");
        sb.append("- タイトル: ").append(recipe.getTitle()).append("\n");
        if (recipe.getDescription() != null) {
            sb.append("- 説明: ").append(recipe.getDescription()).append("\n");
        }
        if (recipe.getCategory() != null) {
            sb.append("- カテゴリ: ").append(recipe.getCategory()).append("\n");
        }
        if (recipe.getConcept() != null) {
            sb.append("- コンセプト: ").append(recipe.getConcept()).append("\n");
        }
        if (recipe.getStory() != null) {
            sb.append("- ストーリー: ").append(recipe.getStory()).append("\n");
        }
        if (recipe.getCookingSteps() != null && !recipe.getCookingSteps().isEmpty()) {
            sb.append("- 調理手順:\n");
            recipe.getCookingSteps().forEach(step ->
                    sb.append("  ").append(step.getStepNumber()).append(". ")
                            .append(step.getDescription()).append("\n"));
        }
        sb.append("\n");
        appendKnowledgeContext(sb, articles);
        return sb.toString();
    }

    public String buildImproveFieldSystemPrompt(List<KnowledgeArticle> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                あなたは感動創出レストランの専門AIアシスタントです。
                レシピの特定のフィールドの内容を改善してください。

                より魅力的で、お客様に感動を届けられる表現に改善してください。
                料理の歴史、文化、ストーリーテリングの観点も考慮してください。

                必ず以下のJSON形式で回答してください。JSON以外のテキストは含めないでください。

                """);
        sb.append("スキーマ:\n```json\n").append(JSON_FIELD_SCHEMA).append("```\n\n");
        appendKnowledgeContext(sb, articles);
        return sb.toString();
    }

    public String buildModifySystemPrompt(List<KnowledgeArticle> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                あなたは感動創出レストランの専門AIアシスタントです。
                ユーザーの修正指示に基づいて、レシピ全体を修正してください。

                指示された部分を修正し、全体の整合性を保ってください。
                料理とサービスを通じてお客様の人生に感動を届けることを意識してください。

                必ず以下のJSON形式で回答してください。JSON以外のテキストは含めないでください。

                """);
        sb.append("スキーマ:\n```json\n").append(JSON_RECIPE_SCHEMA).append("```\n\n");
        appendKnowledgeContext(sb, articles);
        return sb.toString();
    }

    private void appendKnowledgeContext(StringBuilder sb, List<KnowledgeArticle> articles) {
        if (articles != null && !articles.isEmpty()) {
            sb.append("## 参考ナレッジ\n");
            for (KnowledgeArticle article : articles) {
                sb.append("### ").append(article.getTitle()).append("\n");
                sb.append("カテゴリ: ").append(article.getCategory().getName()).append("\n");
                String content = article.getContent();
                if (content.length() > 500) {
                    content = content.substring(0, 500) + "...";
                }
                sb.append(content).append("\n\n");
            }
        }
    }
}
