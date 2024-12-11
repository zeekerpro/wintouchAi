package com.zeeker.service;

import com.zeeker.config.OpenAiConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAiService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);
    private final ChatLanguageModel chatModel;

    public OpenAiService(OpenAiConfig config) {
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();
        logger.info("OpenAI service initialized with model: {}", config.getModel());
    }

    public String generateResponse(String prompt) {
        try {
            return chatModel.generate(prompt);
        } catch (Exception e) {
            logger.error("Error generating response from OpenAI", e);
            throw new RuntimeException("Failed to generate response from OpenAI", e);
        }
    }
}
