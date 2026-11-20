package com.exam.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI testOpenAPI() {
		return new OpenAPI().info(new Info()
				.title("Clients API")
				.version("1.0.0")
				.description("REST API to manage clients."));
	}
}
