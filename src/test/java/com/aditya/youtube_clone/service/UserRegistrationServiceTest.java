package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.dto.UserInfoDTO;
import com.aditya.youtube_clone.model.User;
import com.aditya.youtube_clone.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;

    private UserInfoDTO createUserInfoDTO() {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setGivenName("John");
        dto.setFamilyName("Doe");
        dto.setName("John Doe");
        dto.setEmail("john.doe@example.com");
        return dto;
    }

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Use ReflectionTestUtils instead of manual reflection
        ReflectionTestUtils.setField(userRegistrationService, "userInfoEndpoint",
                mockWebServer.url("/userinfo").toString());

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testRegisterUser_Success() throws JsonProcessingException {
        String token = "valid-token";
        UserInfoDTO userInfoDTO = createUserInfoDTO();
        String responseBody = objectMapper.writeValueAsString(userInfoDTO);

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        userRegistrationService.registerUser(token);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("John Doe", savedUser.getFullName());
        assertEquals("john.doe@example.com", savedUser.getEmailAddress());
    }

    @Test
    public void testRegisterUser_Auth0Returns401() {

        String token = "invalid-token";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userRegistrationService.registerUser(token)
        );

        assertTrue(exception.getMessage().contains("Error registering user"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_Auth0Returns500() {
        String token = "valid-token";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));


        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userRegistrationService.registerUser(token)
        );

        assertTrue(exception.getMessage().contains("Error registering user"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_NetworkTimeout() throws IOException {
        // Arrange
        mockWebServer.shutdown(); // Simulate network failure
        String token = "valid-token";

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userRegistrationService.registerUser(token)
        );

        assertTrue(exception.getMessage().contains("Error registering user"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_DatabaseSaveFailure() throws JsonProcessingException {
        // Arrange
        String token = "valid-token";
        UserInfoDTO userInfoDTO = createUserInfoDTO();
        String responseBody = objectMapper.writeValueAsString(userInfoDTO);

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userRegistrationService.registerUser(token)
        );

        assertTrue(exception.getMessage().contains("Error registering user"));
        assertTrue(exception.getMessage().contains("Database connection failed"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_EmptyToken() throws JsonProcessingException {

        String token = "";
        UserInfoDTO userInfoDTO = createUserInfoDTO();
        String responseBody = objectMapper.writeValueAsString(userInfoDTO);

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        userRegistrationService.registerUser(token);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_NullToken() throws JsonProcessingException {

        UserInfoDTO userInfoDTO = createUserInfoDTO();
        String responseBody = objectMapper.writeValueAsString(userInfoDTO);

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        userRegistrationService.registerUser(null);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_PartialUserInfo() throws JsonProcessingException {
        String token = "valid-token";
        String partialJson = "{\"email\":\"test@example.com\"}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(partialJson)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        userRegistrationService.registerUser(token);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNull(savedUser.getFirstName());
        assertNull(savedUser.getLastName());
        assertNull(savedUser.getFullName());
        assertEquals("test@example.com", savedUser.getEmailAddress());
    }


}