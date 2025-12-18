package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.model.User;
import com.aditya.youtube_clone.service.UserRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService userRegistrationService;
    private User user;
    @BeforeEach
    void initAppContext() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @BeforeEach
    void createUser() {
        this.user = new User();
        this.user.setId("1");
        this.user.setFirstName("testuser");
        this.user.setLastName("testuser");
        this.user.setEmailAddress("test@gmail.com");
        this.user.setSub("abc123");
    }

    @Test
    void testUserRegistration_Success() throws Exception {
        Authentication authentication = mock(Authentication.class);
        doNothing().when(userRegistrationService).registerUser(anyString());
        Jwt jwt = Jwt.withTokenValue("abc123")
                .header("alg", "RS256")
                .claim("sub", "user123")
                .build();
        when(authentication.getPrincipal()).thenReturn(jwt);
        authentication.setAuthenticated(true);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer abc123");
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/user/register")
                        .headers(headers)
                .principal(authentication))
                .andExpect(status().isCreated())
                .andDo(result -> {
                    verify(userRegistrationService, times(1)).registerUser("abc123");
                    String responseContent = result.getResponse().getContentAsString();
                    JSONObject response = new JSONObject(responseContent);
                    assertEquals(1, response.length());
                    assert(response.getString("message").equals("User registered successfully"));
                });
    }
}
