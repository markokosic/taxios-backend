package com.markokosic.minicrm.common.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniCRM API")
                        .version("1.0.0")
                        .description("API Documentation for MiniCRM Application"))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .name("accessToken")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .description("JWT access token stored in httpOnly cookie 'accessToken'")));
    }

    @Bean
    public OpenApiCustomizer defaultJsonResponseCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getResponses() != null) {
                            operation.getResponses().values().forEach(response -> {
                                if (response.getContent() != null && response.getContent().containsKey("*/*")) {
                                    MediaType mediaType = response.getContent().remove("*/*");
                                    response.getContent().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType);
                                }
                            });
                        }
                        if (operation.getParameters() != null) {
                            operation.getParameters().forEach(parameter -> {
                                if ("page".equals(parameter.getName()) && parameter.getSchema() != null) {
                                    parameter.getSchema().setMinimum(java.math.BigDecimal.ONE);
                                    parameter.getSchema().setDefault(1);
                                    parameter.setDescription("Page number (1-indexed, minimum 1)");
                                }
                            });
                        }
                    })
                );
            }
        };
    }
}
