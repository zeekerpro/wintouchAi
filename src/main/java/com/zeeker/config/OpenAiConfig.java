package com.zeeker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class OpenAiConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiConfig.class);
    
    private final String orgId;
    private final String projId;
    private final String apiKey;

    private OpenAiConfig(String orgId, String projId, String apiKey) {
        this.orgId = orgId;
        this.projId = projId;
        this.apiKey = apiKey;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getProjId() {
        return projId;
    }

    public String getApiKey() {
        return apiKey;
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
            
            if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
                throw new IOException("API key not configured in application.yml");
            }

            // orgId 和 projId 可以为空
            if (orgId != null && "your-organization-id-here".equals(orgId)) {
                orgId = null;
            }
            if (projId != null && "your-project-id-here".equals(projId)) {
                projId = null;
            }

            OpenAiConfig openAiConfig = new OpenAiConfig(orgId, projId, apiKey);
            logger.debug("Configuration loaded - orgId: {}, projId: {}", 
                    orgId != null ? orgId : "not set",
                    projId != null ? projId : "not set");
            
            return openAiConfig;
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing YAML configuration: " + e.getMessage(), e);
        }
    }
}
