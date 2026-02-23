package com.recipe.manager.ai;

public interface LlmClient {

    String chat(String systemPrompt, String userMessage);
}
