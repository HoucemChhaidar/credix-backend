package com.asmtunis.credix.backend.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

	@Value("${server.port:8080}")
	private String serverPort;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Credix Backend API")
						.description("Enterprise Credit Distribution System with Barcode Payments")
						.version("1.0.0")
						.contact(new Contact()
								.name("ASM Tunis")
								.email("contact@asmtunis.com")
								.url("https://asmtunis.com"))
						.license(new License()
								.name("MIT License")
								.url("https://opensource.org/licenses/MIT")))
				.servers(List.of(
						new Server()
								.url("http://localhost:" + serverPort)
								.description("Development Server"),
						new Server()
								.url("https://api.credix.asmtunis.com")
								.description("Production Server")))
				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new Components()
						.addSecuritySchemes("Bearer Authentication",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description("Enter JWT token")));
	}
}
