package org.example.prodcatservice.controllers;


import org.example.prodcatservice.models.Product;
import org.example.prodcatservice.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testGetProductById() throws Exception {
        Product product = new Product();
        product.setTitle("Phone");
        product.setPrice(999.99);
        product = productRepository.save(product);

        mockMvc.perform(get("/products/" + product.getId())
                        .header("Authorization", "Bearer dummy-jwt-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Phone")));
    }
}

