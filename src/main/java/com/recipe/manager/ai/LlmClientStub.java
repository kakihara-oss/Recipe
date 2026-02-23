package com.recipe.manager.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "stub", matchIfMissing = true)
public class LlmClientStub implements LlmClient {

    @Override
    public String chat(String systemPrompt, String userMessage) {
        log.info("LlmClientStub called - systemPrompt length: {}, userMessage length: {}",
                systemPrompt.length(), userMessage.length());

        return "【AI相談機能スタブ応答】\n\n"
                + "ご相談ありがとうございます。現在はスタブモードで動作しています。\n"
                + "実際のLLM APIが設定されると、ナレッジベースの情報を活用した具体的なアドバイスをお届けします。\n\n"
                + "ご質問の内容: " + summarize(userMessage);
    }

    private String summarize(String message) {
        if (message.length() <= 100) {
            return message;
        }
        return message.substring(0, 100) + "...";
    }
}
