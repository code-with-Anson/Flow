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
                        .title("心流后端API")
                        .version("1.0")
                        .description("心流后端API文档")
                        .contact(new Contact()
                                .name("Anson&Akatsuki&Sakura")
                                .email("anson@example.com")));
    }
}
