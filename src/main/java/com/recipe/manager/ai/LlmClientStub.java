package com.recipe.manager.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "stub", matchIfMissing = true)
public class LlmClientStub implements LlmClient {

    private static final String STUB_RECIPE_JSON = """
            {
              "title": "春野菜のプリマヴェーラ〜新緑の感動〜",
              "description": "旬の春野菜をふんだんに使った、色鮮やかなパスタ料理です。お客様に春の訪れと自然の恵みへの感動をお届けします。",
              "category": "イタリアン",
              "servings": 4,
              "concept": "春の息吹を五感で感じていただく、季節限定の特別な一皿",
              "story": "冬の長い眠りから覚めた大地が、最初に届けてくれる贈り物。農家さんが丁寧に育てた春野菜を、シンプルながら最大限に活かす調理法で仕上げました。",
              "cookingSteps": [
                {
                  "stepNumber": 1,
                  "description": "春野菜（アスパラガス、スナップエンドウ、菜の花）を下処理し、塩水で軽くブランチングする",
                  "durationMinutes": 5,
                  "temperature": "沸騰",
                  "tips": "野菜の色を鮮やかに保つため、氷水にとる"
                },
                {
                  "stepNumber": 2,
                  "description": "パスタを表示時間より1分短めに茹でる",
                  "durationMinutes": 8,
                  "temperature": "沸騰",
                  "tips": "茹で汁は乳化用に取っておく"
                },
                {
                  "stepNumber": 3,
                  "description": "オリーブオイルでにんにくを香り出しし、春野菜を軽くソテー",
                  "durationMinutes": 3,
                  "temperature": "中火",
                  "tips": "野菜のシャキシャキ感を残すこと"
                },
                {
                  "stepNumber": 4,
                  "description": "パスタと茹で汁を加え、パルミジャーノ・レッジャーノで仕上げる",
                  "durationMinutes": 2,
                  "temperature": "中火",
                  "tips": "茹で汁とオイルをしっかり乳化させる"
                }
              ],
              "ingredients": [
                { "name": "スパゲッティ", "quantity": "320g", "preparationNote": "" },
                { "name": "アスパラガス", "quantity": "8本", "preparationNote": "根元の硬い部分を除く" },
                { "name": "スナップエンドウ", "quantity": "12個", "preparationNote": "筋を取る" },
                { "name": "菜の花", "quantity": "1束", "preparationNote": "3cm幅に切る" },
                { "name": "にんにく", "quantity": "2片", "preparationNote": "みじん切り" },
                { "name": "オリーブオイル", "quantity": "大さじ3", "preparationNote": "" },
                { "name": "パルミジャーノ・レッジャーノ", "quantity": "40g", "preparationNote": "すりおろす" }
              ]
            }
            """;

    private static final String STUB_FIELD_IMPROVE_JSON = """
            {
              "fieldName": "%s",
              "improvedValue": "%s — お客様の心に響く、感動を生む一皿として。季節の移ろいと食材の物語を大切にし、五感すべてに訴えかける体験をお届けします。",
              "explanation": "より具体的な感動ポイントを追加し、お客様の体験に焦点を当てた表現に改善しました。"
            }
            """;

    @Override
    public String chat(String systemPrompt, String userMessage) {
        log.info("LlmClientStub called - systemPrompt length: {}, userMessage length: {}",
                systemPrompt.length(), userMessage.length());

        if (systemPrompt.contains("JSON形式")) {
            if (systemPrompt.contains("improvedValue") || userMessage.contains("フィールド名:")) {
                String fieldName = extractFieldName(userMessage);
                String currentValue = extractCurrentValue(userMessage);
                return String.format(STUB_FIELD_IMPROVE_JSON, fieldName, currentValue);
            }
            return STUB_RECIPE_JSON;
        }

        return "【AI相談機能スタブ応答】\n\n"
                + "ご相談ありがとうございます。現在はスタブモードで動作しています。\n"
                + "実際のLLM APIが設定されると、ナレッジベースの情報を活用した具体的なアドバイスをお届けします。\n\n"
                + "ご質問の内容: " + summarize(userMessage);
    }

    private String extractFieldName(String message) {
        if (message.contains("フィールド名:")) {
            int start = message.indexOf("フィールド名:") + "フィールド名:".length();
            int end = message.indexOf("\n", start);
            if (end > start) {
                return message.substring(start, end).trim();
            }
        }
        return "description";
    }

    private String extractCurrentValue(String message) {
        if (message.contains("現在の値:")) {
            int start = message.indexOf("現在の値:") + "現在の値:".length();
            int end = message.indexOf("\n", start);
            if (end > start) {
                return message.substring(start, end).trim();
            }
        }
        return "（スタブ改善テキスト）";
    }

    private String summarize(String message) {
        if (message.length() <= 100) {
            return message;
        }
        return message.substring(0, 100) + "...";
    }
}
