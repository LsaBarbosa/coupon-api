package com.santanna.couponapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI couponApiOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Coupon API")
                .description("Technical challenge API for coupon creation and soft deletion")
                .version("v1")
                .contact(new Contact().name("Candidate Delivery")));
    }
}
