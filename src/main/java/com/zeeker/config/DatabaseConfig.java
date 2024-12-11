package com.zeeker.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Data
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    
    // Druid specific configurations with default values
    private int initialSize = 5;
    private int minIdle = 5;
    private int maxActive = 20;
    private int maxWait = 60000;
    private int timeBetweenEvictionRunsMillis = 60000;
    private int minEvictableIdleTimeMillis = 300000;
    private String validationQuery = "SELECT 1";
    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean poolPreparedStatements = true;
    private int maxPoolPreparedStatementPerConnectionSize = 20;
    private String filters = "stat,wall";
    private String connectionProperties = "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000";

    public static DatabaseConfig load() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (input == null) {
                throw new IOException("Unable to find application.yml");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);
            Map<String, Object> dbConfig = (Map<String, Object>) config.get("database");

            if (dbConfig == null) {
                throw new IOException("Database configuration section not found in application.yml");
            }

            DatabaseConfig databaseConfig = new DatabaseConfig();
            
            // Required configurations
            databaseConfig.setUrl(getRequiredString(dbConfig, "url"));
            databaseConfig.setUsername(getRequiredString(dbConfig, "username"));
            databaseConfig.setPassword(getRequiredString(dbConfig, "password"));
            databaseConfig.setDriverClassName(getRequiredString(dbConfig, "driverClassName"));
            
            // Optional Druid configurations with defaults
            databaseConfig.setInitialSize(getOptionalInt(dbConfig, "initialSize", databaseConfig.getInitialSize()));
            databaseConfig.setMinIdle(getOptionalInt(dbConfig, "minIdle", databaseConfig.getMinIdle()));
            databaseConfig.setMaxActive(getOptionalInt(dbConfig, "maxActive", databaseConfig.getMaxActive()));
            databaseConfig.setMaxWait(getOptionalInt(dbConfig, "maxWait", databaseConfig.getMaxWait()));
            databaseConfig.setTimeBetweenEvictionRunsMillis(getOptionalInt(dbConfig, "timeBetweenEvictionRunsMillis", databaseConfig.getTimeBetweenEvictionRunsMillis()));
            databaseConfig.setMinEvictableIdleTimeMillis(getOptionalInt(dbConfig, "minEvictableIdleTimeMillis", databaseConfig.getMinEvictableIdleTimeMillis()));
            databaseConfig.setValidationQuery(getOptionalString(dbConfig, "validationQuery", databaseConfig.getValidationQuery()));
            databaseConfig.setTestWhileIdle(getOptionalBoolean(dbConfig, "testWhileIdle", databaseConfig.isTestWhileIdle()));
            databaseConfig.setTestOnBorrow(getOptionalBoolean(dbConfig, "testOnBorrow", databaseConfig.isTestOnBorrow()));
            databaseConfig.setTestOnReturn(getOptionalBoolean(dbConfig, "testOnReturn", databaseConfig.isTestOnReturn()));
            databaseConfig.setPoolPreparedStatements(getOptionalBoolean(dbConfig, "poolPreparedStatements", databaseConfig.isPoolPreparedStatements()));
            databaseConfig.setMaxPoolPreparedStatementPerConnectionSize(getOptionalInt(dbConfig, "maxPoolPreparedStatementPerConnectionSize", databaseConfig.getMaxPoolPreparedStatementPerConnectionSize()));
            databaseConfig.setFilters(getOptionalString(dbConfig, "filters", databaseConfig.getFilters()));
            databaseConfig.setConnectionProperties(getOptionalString(dbConfig, "connectionProperties", databaseConfig.getConnectionProperties()));

            logger.info("Database configuration loaded successfully");
            return databaseConfig;
        } catch (IOException e) {
            logger.error("Error loading database configuration", e);
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    private static String getRequiredString(Map<String, Object> config, String key) throws IOException {
        String value = (String) config.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("Required configuration '" + key + "' is missing or empty");
        }
        return value;
    }

    private static String getOptionalString(Map<String, Object> config, String key, String defaultValue) {
        return Optional.ofNullable(config.get(key))
                .map(Object::toString)
                .orElse(defaultValue);
    }

    private static int getOptionalInt(Map<String, Object> config, String key, int defaultValue) {
        return Optional.ofNullable(config.get(key))
                .map(value -> {
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    try {
                        return Integer.parseInt(value.toString());
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number format for key '{}': {}, using default value: {}", key, value, defaultValue);
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private static boolean getOptionalBoolean(Map<String, Object> config, String key, boolean defaultValue) {
        return Optional.ofNullable(config.get(key))
                .map(value -> {
                    if (value instanceof Boolean) {
                        return (Boolean) value;
                    }
                    return Boolean.parseBoolean(value.toString());
                })
                .orElse(defaultValue);
    }
}
