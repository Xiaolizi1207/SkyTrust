package com.skytrust.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "skytrust.jwt.secret=integration-test-secret-key",
    "skytrust.blockchain.enabled=false",
    "skytrust.ai.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void docHtml_shouldBePublic() throws Exception {
        mockMvc.perform(get("/doc.html"))
                .andExpect(status().isOk());
    }

    @Test
    void publicAuthEndpoints_shouldReachController() throws Exception {
        String[] paths = {"/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/captcha"};
        for (String path : paths) {
            mockMvc.perform(post(path)
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(result -> {
                        int code = result.getResponse().getStatus();
                        assert code != 401 : path + " should not require auth but got 401";
                    });
        }
    }

    @Test
    void swaggerResources_shouldBePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    assert code != 401 : "Swagger should be public but got 401";
                });
    }
}
