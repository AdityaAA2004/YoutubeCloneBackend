package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRegistrationService userRegistrationService;

    @GetMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> registerUser(Authentication authentication) {
        Jwt jwtToken = (Jwt) authentication.getPrincipal();
        String token = jwtToken.getTokenValue();
        this.userRegistrationService.registerUser(token);
        try {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON).body(jsonObject.toString());
        } catch (JSONException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error creating JSON response" + e.getMessage());
        }
    }
}
