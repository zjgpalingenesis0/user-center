package com.zjg.usercenter.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@Profile("prod")
public class SwaggerConfig {
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info()
                .title("用户中心")
                .description("用户中心接口文档")
                .version("v1.0.0")
                .contact(new Contact().name("zjg")));
    }
}
