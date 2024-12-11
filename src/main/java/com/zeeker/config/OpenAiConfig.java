package com.zeeker.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Data
public class OpenAiConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiConfig.class);
    
    private final String orgId;
    private final String projId;
    private final String apiKey;
    private final String model;
    private final double temperature;
    private final int maxTokens;

    private OpenAiConfig(String orgId, String projId, String apiKey, String model, double temperature, int maxTokens) {
        this.orgId = orgId;
        this.projId = projId;
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public static OpenAiConfig load() {
        try (InputStream input = OpenAiConfig.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (input == null) {
                throw new IOException("Unable to find application.yml");
            }
            
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);
            Map<String, Object> openai = (Map<String, Object>) config.get("openai");
            
            String orgId = (String) openai.get("orgId");
            String projId = (String) openai.get("projId");
            String apiKey = (String) openai.get("apiKey");
            String model = (String) openai.get("model");
            double temperature = ((Number) openai.get("temperature")).doubleValue();
            int maxTokens = ((Number) openai.get("maxTokens")).intValue();
            
            if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
                throw new IOException("API key not configured in application.yml");
            }

            if (orgId != null && "your-organization-id-here".equals(orgId)) {
                orgId = null;
            }
            if (projId != null && "your-project-id-here".equals(projId)) {
                projId = null;
            }

            OpenAiConfig openAiConfig = new OpenAiConfig(orgId, projId, apiKey, model, temperature, maxTokens);
            logger.info("OpenAI configuration loaded with model: {}", model);
            
            return openAiConfig;
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing YAML configuration: " + e.getMessage(), e);
        }
    }
}
