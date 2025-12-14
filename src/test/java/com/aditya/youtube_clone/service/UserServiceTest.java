package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.model.User;
import com.aditya.youtube_clone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User existingUser;

    private Jwt mockJwt;

    private Authentication mockAuthentication;
    private SecurityContext mockSecurityContext;

    @BeforeEach
    void setupExistingUser() {
        existingUser = new User();
        existingUser.setId("user123");
        existingUser.setSub("sub123");
        existingUser.setFirstName("firstName");
        existingUser.setLastName("lastName");
        existingUser.setEmailAddress("first@example.com");
        existingUser.setSubscribedToUsers(ConcurrentHashMap.newKeySet());
        existingUser.setDisLikedVideos(ConcurrentHashMap.newKeySet());
        existingUser.setLikedVideos(ConcurrentHashMap.newKeySet());
        existingUser.setVideoHistory(List.of("someVideoId"));
        this.mockJwt = Mockito.mock(Jwt.class);
        this.mockAuthentication = Mockito.mock(Authentication.class);
        this.mockSecurityContext = Mockito.mock(SecurityContext.class);
    }

    @Test
    public void testGetCurrentUser_Success() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));
            User foundUser = userService.getCurrentUser();
            assertNotNull(foundUser);
            assertEquals("user123", foundUser.getId());
            assertEquals("sub123", foundUser.getSub());
            assertEquals("firstName", foundUser.getFirstName());
            assertEquals("lastName", foundUser.getLastName());
            assertEquals("first@example.com", foundUser.getEmailAddress());

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testGetCurrentUser_UserNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.empty());
            try {
                userService.getCurrentUser();
            } catch (RuntimeException ex) {
                assertEquals("User not found with given auth token", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testAddToLikedVideos_Success() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));

            userService.addToLikedVideos("video123");

            verify(userRepository, times(1)).findBySub("sub123");
            verify(userRepository, times(1)).save(existingUser);

            assertEquals(1, existingUser.getLikedVideos().size());
            assertEquals("video123", existingUser.getLikedVideos().iterator().next());
        }
    }
    @Test
    public void testAddToLikedVideos_UserNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.empty());

            try {
                userService.addToLikedVideos("video123");
            } catch (RuntimeException ex) {
                assertEquals("User not found with given auth token", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testAddToLikedVideos_DBErrorOnSave() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));
            doThrow(new RuntimeException("Database error")).when(userRepository).save(existingUser);

            try {
                userService.addToLikedVideos("video123");
            } catch (RuntimeException ex) {
                assertEquals("Database error", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
            verify(userRepository, times(1)).save(existingUser);
            assertEquals(1, existingUser.getLikedVideos().size()); // just the in-memory object
        }
    }
    @Test
    public void testCheckUserLikedVideo_VideoFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            existingUser.getLikedVideos().add("video123");
            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));

            boolean isLiked = userService.checkUserLikedVideo("video123");
            assertTrue(isLiked);

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testCheckUserLikedVideo_VideoNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));

            boolean isLiked = userService.checkUserLikedVideo("video123");
            assertFalse(isLiked);

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testCheckUserLikedVideo_UserNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.empty());

            try {
                userService.checkUserLikedVideo("video123");
            } catch (RuntimeException ex) {
                assertEquals("User not found with given auth token", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testCheckUserDisLikedVideo_VideoFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            existingUser.getDisLikedVideos().add("video123");
            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));

            boolean isDisLiked = userService.checkUserDisLikedVideo("video123");
            assertTrue(isDisLiked);

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testCheckUserDisLikedVideo_VideoNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));

            boolean isDisLiked = userService.checkUserDisLikedVideo("video123");
            assertFalse(isDisLiked);

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testCheckUserDisLikedVideo_UserNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.empty());

            try {
                userService.checkUserDisLikedVideo("video123");
            } catch (RuntimeException ex) {
                assertEquals("User not found with given auth token", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testRemoveFromLikedVideos_Success() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            Set<String> likedVideos = ConcurrentHashMap.newKeySet();
            likedVideos.add("video123");
            existingUser.setLikedVideos(likedVideos);
            assertEquals(1, existingUser.getLikedVideos().size());
            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));
            doReturn(existingUser).when(userRepository).save(existingUser);

            userService.removeFromLikedVideos("video123");

            verify(userRepository, times(1)).findBySub("sub123");
            verify(userRepository, times(1)).save(existingUser);
            assertEquals(0, existingUser.getLikedVideos().size());
        }
    }
    @Test
    public void testRemoveFromLikedVideos_UserNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.empty());

            try {
                userService.removeFromLikedVideos("video123");
            } catch (RuntimeException ex) {
                assertEquals("User not found with given auth token", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testRemoveFromLikedVideos_DBErrorOnSave() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            Set<String> likedVideos = ConcurrentHashMap.newKeySet();
            likedVideos.add("video123");
            existingUser.setLikedVideos(likedVideos);
            assertEquals(1, existingUser.getLikedVideos().size());

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));
            doThrow(new RuntimeException("Database error")).when(userRepository).save(existingUser);

            try {
                userService.removeFromLikedVideos("video123");
            } catch (RuntimeException ex) {
                assertEquals("Database error", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
            verify(userRepository, times(1)).save(existingUser);
            assertEquals(0, existingUser.getLikedVideos().size()); // just the in-memory object
        }
    }
    @Test
    public void testRemoveFromDisLikedVideos_Success() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            Set<String> disLikedVideos = ConcurrentHashMap.newKeySet();
            disLikedVideos.add("video123");
            existingUser.setDisLikedVideos(disLikedVideos);
            assertEquals(1, existingUser.getDisLikedVideos().size());
            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));
            doReturn(existingUser).when(userRepository).save(existingUser);

            userService.removeFromDisLikedVideos("video123");

            verify(userRepository, times(1)).findBySub("sub123");
            verify(userRepository, times(1)).save(existingUser);
            assertEquals(0, existingUser.getDisLikedVideos().size());
        }
    }
    @Test
    public void testRemoveFromDisLikedVideos_UserNotFound() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.empty());

            try {
                userService.removeFromDisLikedVideos("video123");
            } catch (RuntimeException ex) {
                assertEquals("User not found with given auth token", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
        }
    }
    @Test
    public void testRemoveFromDisLikedVideos_DBErrorOnSave() {
        when(mockJwt.getClaim("sub")).thenReturn("sub123");
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            Set<String> disLikedVideos = ConcurrentHashMap.newKeySet();
            disLikedVideos.add("video123");
            existingUser.setDisLikedVideos(disLikedVideos);
            assertEquals(1, existingUser.getDisLikedVideos().size());

            when(userRepository.findBySub(anyString())).thenReturn(java.util.Optional.of(existingUser));
            doThrow(new RuntimeException("Database error")).when(userRepository).save(existingUser);

            try {
                userService.removeFromDisLikedVideos("video123");
            } catch (RuntimeException ex) {
                assertEquals("Database error", ex.getMessage());
            }

            verify(userRepository, times(1)).findBySub("sub123");
            verify(userRepository, times(1)).save(existingUser);
            assertEquals(0, existingUser.getDisLikedVideos().size()); // just the in-memory object
        }
    }
}
