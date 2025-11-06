/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 * <p>
 * https://www.collabtask.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.user.commons.dynamic.datasource.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.user.commons.dynamic.datasource.properties.DataSourceProperties;
import io.user.commons.dynamic.datasource.properties.DynamicDataSourceProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置多数据源
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceConfig.class);
    
    @Resource
    private DynamicDataSourceProperties properties;
    
    @Resource
    private DataSourceProperties dataSourceProperties;

    @PostConstruct
    public void init() {
        logger.info("DynamicDataSourceConfig initialized");
        logger.info("Default DataSourceProperties - URL: {}, Username: {}", dataSourceProperties.getUrl(), dataSourceProperties.getUsername());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties props = new DataSourceProperties();
        logger.info("Creating DataSourceProperties bean");
        return props;
    }

    @Bean
    @org.springframework.context.annotation.Lazy
    public DynamicDataSource dynamicDataSource(DataSourceProperties dataSourceProperties) {
        logger.info("DataSource Properties - URL: {}, Username: {}", dataSourceProperties.getUrl(), dataSourceProperties.getUsername());
        
        // 检查URL是否为空
        if (dataSourceProperties.getUrl() == null || dataSourceProperties.getUrl().isEmpty()) {
            logger.error("DataSource URL is not set!");
            throw new IllegalStateException("DataSource URL is not set!");
        }
        
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(getDynamicDataSource());

        //默认数据源
        DruidDataSource defaultDataSource = DynamicDataSourceFactory.buildDruidDataSource(dataSourceProperties);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

        return dynamicDataSource;
    }

    private Map<Object, Object> getDynamicDataSource() {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>(dataSourcePropertiesMap.size());
        dataSourcePropertiesMap.forEach((k, v) -> {
            logger.info("Dynamic DataSource - Key: {}, URL: {}, Username: {}", k, v.getUrl(), v.getUsername());
            DruidDataSource druidDataSource = DynamicDataSourceFactory.buildDruidDataSource(v);
            targetDataSources.put(k, druidDataSource);
        });

        return targetDataSources;
    }

}