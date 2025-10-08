package com.insp.poc_login.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.*;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "POC Login REST API",
                version = "1.0.0",
                description = "Spring Boot + JWT + OpenAPI 3 Configuration"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

//    @Bean
//    public OpenAPI openAPI() {
//        // Define Bearer Security Scheme
//        SecurityScheme bearerScheme = new SecurityScheme()
//                .type(SecurityScheme.Type.HTTP)
//                .scheme("bearer")
//                .bearerFormat("JWT")
//                .in(SecurityScheme.In.HEADER)
//                .name("Authorization");
//
//        // Define Security Requirement (applied globally)
//        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
//
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Demo REST API")
//                        .description("Spring Boot + JWT + OpenAPI 3 Configuration")
//                        .version("1.0.0"))
//                .components(new Components().addSecuritySchemes("bearerAuth", bearerScheme))
//                .addSecurityItem(securityRequirement);
//    }



}
