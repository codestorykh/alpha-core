package com.codestorykh.alpha.config.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Properties;

@Configuration
public class BuildInfoProperties {

    @Bean
    @ConditionalOnMissingBean(BuildProperties.class)
    public BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "0.0.1-SNAPSHOT");
        properties.setProperty("name", "alpha-core");
        properties.setProperty("time", Instant.now().toString());
        properties.setProperty("group", "com.codestorykh");
        properties.setProperty("artifact", "alpha-core");
        
        return new BuildProperties(properties);
    }
} 