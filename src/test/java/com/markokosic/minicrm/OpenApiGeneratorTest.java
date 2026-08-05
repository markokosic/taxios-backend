package com.markokosic.minicrm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.io.FileWriter;
import java.io.File;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiGeneratorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "SWAGGER_ADMIN")
    void generateOpenApiSpec() throws Exception {
        // Ruft den OpenAPI /v3/api-docs Endpunkt über MockMvc ab
        String openApiJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Schreibt die Datei in den target-Ordner des Backends
        File targetDir = new File("target");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        try (FileWriter fileWriter = new FileWriter("target/openapi.json")) {
            fileWriter.write(openApiJson);
        }
        
        // Kopiert die Datei direkt in das Frontend-Verzeichnis, falls vorhanden
        File frontendDir = new File("../frontend");
        if (frontendDir.exists()) {
            try (FileWriter fileWriter = new FileWriter("../frontend/openapi.json")) {
                fileWriter.write(openApiJson);
            }
        }
    }
}
