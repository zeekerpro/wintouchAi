package com.zeeker.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RagService {
    private static final Logger logger = LoggerFactory.getLogger(RagService.class);
    
    private final DatabaseService databaseService;
    private final OpenAiService openAiService;
    private final String databaseSchema;

    public RagService(DatabaseService databaseService, OpenAiService openAiService) {
        this.databaseService = databaseService;
        this.openAiService = openAiService;
        this.databaseSchema = databaseService.getDatabaseSchemaDescription();
    }

    public String processNaturalLanguageQuery(String question) {
        try {
            // 构建提示词
            String prompt = String.format(
                "Based on the following database schema:\\n\\n%s\\n\\n" +
                "Generate a SQL query to answer this question: %s\\n\\n" +
                "Return ONLY the SQL query, without any markdown formatting, explanations or additional text.",
                databaseSchema, question
            );

            // 生成并清理SQL查询
            String sqlQuery = cleanSqlQuery(openAiService.generateResponse(prompt));
            logger.info("Generated SQL query: {}", sqlQuery);

            // 执行查询
            List<Map<String, Object>> results = databaseService.executeQuery(sqlQuery);

            // 构建结果解释提示词
            String resultPrompt = String.format(
                "Based on the question: %s\\n\\n" +
                "And the query results:\\n%s\\n\\n" +
                "Please provide a natural language answer to the question.",
                question, formatResults(results)
            );

            // 生成自然语言回答
            String answer = openAiService.generateResponse(resultPrompt);
            logger.info("Generated answer: {}", answer);

            return answer;

        } catch (Exception e) {
            logger.error("Error processing natural language query", e);
            return "Sorry, I encountered an error while processing your question: " + e.getMessage();
        }
    }

    private String cleanSqlQuery(String rawQuery) {
        // 移除markdown代码块标记
        String cleaned = rawQuery.replaceAll("```sql\\s*", "")
                               .replaceAll("```", "")
                               .trim();
        
        // 移除多余的空白字符
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        // 确保以分号结尾
        if (!cleaned.endsWith(";")) {
            cleaned += ";";
        }
        
        return cleaned;
    }

    private String formatResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "No results found.";
        }

        StringBuilder sb = new StringBuilder();
        // 添加表头
        sb.append(String.join(", ", results.get(0).keySet())).append("\\n");
        
        // 添加数据行
        for (Map<String, Object> row : results) {
            sb.append(String.join(", ", 
                row.values().stream()
                    .map(value -> value == null ? "null" : value.toString())
                    .toList()
            )).append("\\n");
        }
        
        return sb.toString();
    }
}
