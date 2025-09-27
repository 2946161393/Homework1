package com.example.employee.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class})
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("handles validation errors with detailed response")
    void handles_validation_errors() throws Exception {
        TestDto invalidDto = new TestDto("", "invalid-email");

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.service").value("employee-service"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    @DisplayName("handles ResourceNotFoundException")
    void handles_resource_not_found() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpected(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.service").value("employee-service"))
                .andExpect(jsonPath("$.resourceType").value("Employee"))
                .andExpect(jsonPath("$.resourceId").value("999"));
    }

    @Test
    @DisplayName("handles DuplicateResourceException")
    void handles_duplicate_resource() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Resource"))
                .andExpect(jsonPath("$.service").value("employee-service"))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("handles ExternalServiceException")
    void handles_external_service_error() throws Exception {
        mockMvc.perform(get("/test/external-error"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("External Service Error"))
                .andExpect(jsonPath("$.service").value("employee-service"))
                .andExpect(jsonPath("$.externalService").value("department-service"));
    }

    @Test
    @DisplayName("handles generic exceptions")
    void handles_generic_exception() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.service").value("employee-service"));
    }

    @RestController
    static class TestController {

        @PostMapping("/test/validate")
        public String validate(@Valid @RequestBody TestDto dto) {
            return "OK";
        }

        @GetMapping("/test/not-found")
        public String notFound() {
            throw new ResourceNotFoundException("Employee", 999L);
        }

        @GetMapping("/test/duplicate")
        public String duplicate() {
            throw new DuplicateResourceException("Employee", "test@example.com");
        }

        @GetMapping("/test/external-error")
        public String externalError() {
            throw new ExternalServiceException("department-service", "Service unavailable", 502);
        }

        @GetMapping("/test/generic-error")
        public String genericError() {
            throw new RuntimeException("Something went wrong");
        }
    }

    static class TestDto {
        @NotBlank
        private String name;

        @Email
        private String email;

        public TestDto(String name, String email) {
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}