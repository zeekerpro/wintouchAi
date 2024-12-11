package com.zeeker.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.zeeker.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private final DruidDataSource dataSource;
    private final Map<String, List<String>> tableColumns = new HashMap<>();
    private final Map<String, String> columnTypes = new HashMap<>();

    public DatabaseService(DatabaseConfig config) {
        this.dataSource = new DruidDataSource();
        
        // 基本配置
        dataSource.setUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setDriverClassName(config.getDriverClassName());
        
        // 连接池配置
        dataSource.setInitialSize(config.getInitialSize());
        dataSource.setMinIdle(config.getMinIdle());
        dataSource.setMaxActive(config.getMaxActive());
        dataSource.setMaxWait(config.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(config.getValidationQuery());
        dataSource.setTestWhileIdle(config.isTestWhileIdle());
        dataSource.setTestOnBorrow(config.isTestOnBorrow());
        dataSource.setTestOnReturn(config.isTestOnReturn());
        dataSource.setPoolPreparedStatements(config.isPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(config.getMaxPoolPreparedStatementPerConnectionSize());
        
        try {
            dataSource.setFilters(config.getFilters());
            dataSource.setConnectionProperties(config.getConnectionProperties());
        } catch (SQLException e) {
            logger.error("Error setting Druid filters", e);
            throw new RuntimeException("Failed to configure Druid filters", e);
        }

        loadDatabaseSchema();
    }

    private void loadDatabaseSchema() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                List<String> columns = new ArrayList<>();
                ResultSet columnsRs = metaData.getColumns(catalog, null, tableName, "%");

                while (columnsRs.next()) {
                    String columnName = columnsRs.getString("COLUMN_NAME");
                    String columnType = columnsRs.getString("TYPE_NAME");
                    columns.add(columnName);
                    columnTypes.put(tableName + "." + columnName, columnType);
                }

                tableColumns.put(tableName, columns);
                logger.info("Loaded schema for table: {} with columns: {}", tableName, columns);
            }
        } catch (SQLException e) {
            logger.error("Error loading database schema", e);
            throw new RuntimeException("Failed to load database schema", e);
        }
    }

    public String getDatabaseSchemaDescription() {
        StringBuilder schema = new StringBuilder();
        schema.append("Database Schema:\\n");
        
        for (Map.Entry<String, List<String>> table : tableColumns.entrySet()) {
            schema.append("Table: ").append(table.getKey()).append("\\n");
            schema.append("Columns:\\n");
            
            for (String column : table.getValue()) {
                String type = columnTypes.get(table.getKey() + "." + column);
                schema.append("  - ").append(column).append(" (").append(type).append(")\\n");
            }
            schema.append("\\n");
        }
        
        return schema.toString();
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
            
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }
        
        return results;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
