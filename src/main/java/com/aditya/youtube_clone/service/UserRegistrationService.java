package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.dto.UserInfoDTO;
import com.aditya.youtube_clone.model.User;
import com.aditya.youtube_clone.repository.UserRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    @Value("${auth0.userinfoEndpoint}")
    private String userInfoEndpoint;

    private final UserRepository userRepository;

    public void registerUser(String token) {
        try(HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build()) {

            // Fetch user info from Auth0
            HttpRequest userInfoRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(java.net.URI.create(userInfoEndpoint))
                    .setHeader("Authorization", String.format("Bearer %s", token))
                    .build();
            HttpResponse<String> userInfoResponse = httpClient.send(userInfoRequest,
                    HttpResponse.BodyHandlers.ofString());
            String userInfoResponseBody = userInfoResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            UserInfoDTO userInfo = objectMapper.readValue(userInfoResponseBody, UserInfoDTO.class);

            // Save user info to database
            User user = new User();
            user.setFirstName(userInfo.getGivenName());
            user.setLastName(userInfo.getFamilyName());
            user.setFullName(userInfo.getName());
            user.setEmailAddress(userInfo.getEmail());

            userRepository.save(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error registering user: " + e.getMessage());
        }

    }
}
