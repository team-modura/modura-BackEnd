package com.modura.modura_server;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication
@EnableJpaAuditing
public class ModuraServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuraServerApplication.class, args);
	}
}