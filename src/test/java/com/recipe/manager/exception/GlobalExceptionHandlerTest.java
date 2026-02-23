package com.recipe.manager.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test/not-found")
        public ResponseEntity<Void> throwNotFound() {
            throw new ResourceNotFoundException("TestResource", 1L);
        }

        @GetMapping("/api/test/business-error")
        public ResponseEntity<Void> throwBusinessLogic() {
            throw new BusinessLogicException("Business rule violated");
        }

        @GetMapping("/api/test/forbidden")
        public ResponseEntity<Void> throwForbidden() {
            throw new ForbiddenException("Access denied");
        }
    }

    @Test
    void ResourceNotFoundException_404が返る() throws Exception {
        mockMvc.perform(get("/api/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("TestResource not found with id: 1"));
    }

    @Test
    void BusinessLogicException_400が返る() throws Exception {
        mockMvc.perform(get("/api/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business rule violated"));
    }

    @Test
    void ForbiddenException_403が返る() throws Exception {
        mockMvc.perform(get("/api/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }
}
