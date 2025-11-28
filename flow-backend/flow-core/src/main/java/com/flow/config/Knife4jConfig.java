package com.flow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flow Backend API")
                        .version("1.0")
                        .description("Personal Multimodal Digital Memory - Backend API Documentation")
                        .contact(new Contact()
                                .name("Anson")
                                .email("anson@example.com")));
    }
}
