package com.codestorykh.alpha.config;

import com.codestorykh.alpha.config.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurationInitializer implements CommandLineRunner {

    private final ConfigurationService configurationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting configuration initialization...");
        
        try {
            configurationService.initializeDefaultConfigurations();
            log.info("Configuration initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize configurations", e);
            // Don't throw the exception to allow the application to start
        }
    }
} 