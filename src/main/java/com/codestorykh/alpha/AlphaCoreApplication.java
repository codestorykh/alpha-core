package com.codestorykh.alpha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class AlphaCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlphaCoreApplication.class, args);
	}

}
