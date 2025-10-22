package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private VideoService videoService;

    @Test
    public void uploadVideoTest_Success() throws IOException {
        // Arrange
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(s3Service.uploadFile(any())).thenReturn("s3-url");

        // Act
        videoService.uploadVideo(mockMultipartFile);

        // Assert
        verify(s3Service, times(1)).uploadFile(any());
        verify(videoRepository, times(1)).save(any());
    }

    @Test
    public void uploadVideoTest_S3UploadFailure() throws IOException {
        // Arrange
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(s3Service.uploadFile(any())).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"S3 upload failed"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            videoService.uploadVideo(mockMultipartFile);
        });

        verify(s3Service, times(1)).uploadFile(any());
        verify(videoRepository, times(0)).save(any());
    }
}