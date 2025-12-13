package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.model.User;
import com.aditya.youtube_clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Jwt jwtToken = (Jwt) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String userSub = jwtToken.getClaim("sub").toString();
        return userRepository.findBySub(userSub).orElseThrow(
                () -> new RuntimeException("User not found with given auth token")
        );
    }

    public void addToLikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.addToLikedVideos(videoId);
        userRepository.save(currentUser);
    }

    public boolean checkUserLikedVideo(String videoId) {
        return getCurrentUser().getLikedVideos().stream().anyMatch(v -> v.equals(videoId));
    }

    public boolean checkUserDisLikedVideo(String videoId) {
        return getCurrentUser().getDisLikedVideos().stream().anyMatch(v -> v.equals(videoId));
    }

    public void removeFromLikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.removeFromLikedVideos(videoId);
        userRepository.save(currentUser);
    }

    public void removeFromDisLikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.removeFromDisLikedVideos(videoId);
        userRepository.save(currentUser);
    }
}
