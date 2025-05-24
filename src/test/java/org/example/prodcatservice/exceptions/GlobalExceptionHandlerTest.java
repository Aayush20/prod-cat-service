package org.example.prodcatservice.exceptions;

import org.example.prodcatservice.dtos.product.responseDtos.ResponseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandlerTest.DummyController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class DummyController {

        @GetMapping("/runtime")
        public void throwRuntime() {
            throw new RuntimeException("Something went wrong!");
        }

        @GetMapping("/general")
        public void throwException() throws Exception {
            throw new Exception("Unexpected failure!");
        }

        @PostMapping("/validate")
        public void validateInput(@RequestBody DummyRequest body) {
            // Will trigger MethodArgumentNotValidException if body is invalid
        }
    }

    static class DummyRequest {
        @jakarta.validation.constraints.NotBlank
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Test
    void testRuntimeExceptionHandled() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.statusType").value(ResponseStatus.FAILURE.toString()));
    }

    @Test
    void testGeneralExceptionHandled() throws Exception {
        mockMvc.perform(get("/test/general"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.statusType").value(ResponseStatus.FAILURE.toString()));
    }

    @Test
    void testValidationExceptionHandled() throws Exception {
        String invalidJson = "{}"; // missing 'name' field

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.statusType").value(ResponseStatus.FAILURE.toString()));
    }
}
