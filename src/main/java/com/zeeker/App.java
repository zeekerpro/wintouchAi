package com.zeeker;

import com.zeeker.config.OpenAiConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            OpenAiConfig config = OpenAiConfig.load();
            logger.info("Loaded configuration - Organization ID: {}", 
                       config.getOrgId());

            OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(GPT_4_O_MINI)
                .build();

            try {
                String answer = model.generate("Say 'Hello World'");
                logger.info("AI Response: {}", answer);
            } catch (Exception e) {
                logger.error("Error while generating response: ", e);
            }
        } catch (RuntimeException e) {
            logger.error("Configuration error: ", e);
        }
    }
}
