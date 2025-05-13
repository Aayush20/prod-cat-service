package org.example.prodcatservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryAuditLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAccessDeniedForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/inventory-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-jwt-without-admin-role"))
                .andExpect(status().isForbidden());
    }

    // You can inject a real signed JWT or mock it using @WithMockUser in unit tests
}
