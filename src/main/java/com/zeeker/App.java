package com.zeeker;

import com.zeeker.config.DatabaseConfig;
import com.zeeker.config.OpenAiConfig;
import com.zeeker.service.DatabaseService;
import com.zeeker.service.OpenAiService;
import com.zeeker.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            // 加载配置
            OpenAiConfig openAiConfig = OpenAiConfig.load();
            DatabaseConfig dbConfig = DatabaseConfig.load();
            
            logger.info("Loaded OpenAI configuration");
            logger.info("Loaded database configuration - URL: {}", dbConfig.getUrl());

            // 初始化服务
            DatabaseService databaseService = new DatabaseService(dbConfig);
            OpenAiService openAiService = new OpenAiService(openAiConfig);
            RagService ragService = new RagService(databaseService, openAiService);

            // 启动交互式命令行
            logger.info("Starting interactive session. Type 'exit' to quit.");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("\nEnter your question (or 'exit' to quit): ");
                String question = reader.readLine();

                if (question == null || "exit".equalsIgnoreCase(question.trim())) {
                    logger.info("Exiting application...");
                    break;
                }

                if (question.trim().isEmpty()) {
                    System.out.println("Please enter a valid question.");
                    continue;
                }

                String answer = ragService.processNaturalLanguageQuery(question);
                System.out.println("\nAnswer: " + answer);
            }

            // 关闭资源
            databaseService.close();
            logger.info("Application terminated successfully");
            System.exit(0);

        } catch (Exception e) {
            logger.error("Application error: ", e);
            System.exit(1);
        }
    }
}
