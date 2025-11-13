package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.dto.VideoDTO;
import com.aditya.youtube_clone.dto.VideoUploadResponseDTO;
import com.aditya.youtube_clone.model.Video;
import com.aditya.youtube_clone.model.VideoStatus;
import com.aditya.youtube_clone.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(s3Service.uploadFile(any())).thenReturn("s3-url");
        Video savedVideo = new Video();
        savedVideo.setId("1");
        savedVideo.setVideoUrl("s3-url");
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        VideoUploadResponseDTO uploadedVideo = videoService.uploadVideo(mockMultipartFile);
        verify(s3Service, times(1)).uploadFile(any());
        verify(videoRepository, times(1)).save(any());
        assertEquals("s3-url", uploadedVideo.getVideoUrl());
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

    @Test
    public void editVideoTest_Success() {
        Video video = new Video();
        video.setId("videoId");
        video.setTitle("title");
        video.setDescription("description");
        video.setVideoStatus(VideoStatus.PUBLIC);
        video.setVideoUrl("http://example.com/video");
        video.setThumbnailUrl("http://example.com/thumbnail");
        video.setLikes(0);
        video.setDisLikes(0);
        when(videoRepository.findById(any())).thenReturn(java.util.Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenReturn(video);
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId("videoId");
        videoDTO.setTitle("new title");
        videoDTO.setDescription("new description");
        videoDTO.setVideoStatus(VideoStatus.PRIVATE);
        videoDTO.setVideoUrl("http://example.com/video");
        videoDTO.setThumbnailUrl("http://example.com/new_thumbnail");
        VideoDTO editedVideo = videoService.editVideo(videoDTO);
        verify(videoRepository, times(1)).findById("videoId");
        verify(videoRepository, times(1)).save(any(Video.class));
        assertEquals(editedVideo.getTitle(), videoDTO.getTitle());
        assertEquals(editedVideo.getDescription(), videoDTO.getDescription());
        assertEquals(editedVideo.getVideoStatus(), videoDTO.getVideoStatus());
        assertEquals(editedVideo.getThumbnailUrl(), videoDTO.getThumbnailUrl());
        assertEquals(editedVideo.getVideoStatus(), videoDTO.getVideoStatus());
    }

    @Test
    public void editVideoTest_VideoNotFound() {
        when(videoRepository.findById(any())).thenReturn(java.util.Optional.empty());
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId("nonExistentVideoId");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            videoService.editVideo(videoDTO);
        });
        verify(videoRepository, times(1)).findById("nonExistentVideoId");
        verify(videoRepository, times(0)).save(any(Video.class));
        assertEquals("Cannot find video by ID: nonExistentVideoId", exception.getMessage());
    }

    @Test
    public void editVideoTest_DB_Failure() {
        Video video = new Video();
        video.setId("videoId");
        video.setTitle("title");
        when(videoRepository.findById(any())).thenReturn(java.util.Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenThrow(new RuntimeException("DB error"));
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId("videoId");
        videoDTO.setTitle("new title");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            videoService.editVideo(videoDTO);
        });
        verify(videoRepository, times(1)).findById("videoId");
        verify(videoRepository, times(1)).save(any(Video.class));
        assertEquals("DB error", exception.getMessage());
    }

    @Test
    public void uploadThumbnailTest_Success() throws IOException {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        Video video = new Video();
        video.setId("videoId");
        when(videoRepository.findById(any())).thenReturn(java.util.Optional.of(video));
        when(s3Service.uploadFile(any())).thenReturn("s3-thumbnail-url");
        when(videoRepository.save(any(Video.class))).thenReturn(video);
        String thumbnailUrl = videoService.uploadThumbnail(mockMultipartFile, "videoId");
        verify(videoRepository, times(1)).findById("videoId");
        verify(s3Service, times(1)).uploadFile(any());
        verify(videoRepository, times(1)).save(any());
        assertEquals("s3-thumbnail-url", thumbnailUrl);
    }

    @Test
    public void uploadThumbnailTest_VideoNotFound() throws IOException {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(videoRepository.findById(any())).thenReturn(java.util.Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            videoService.uploadThumbnail(mockMultipartFile, "nonExistentVideoId");
        });
        verify(videoRepository, times(1)).findById("nonExistentVideoId");
        verify(s3Service, times(0)).uploadFile(any());
        verify(videoRepository, times(0)).save(any());
        assertEquals("Cannot find video by ID: nonExistentVideoId", exception.getMessage());
    }

    @Test
    public void uploadThumbnailTest_S3UploadFailure() throws IOException {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        Video video = new Video();
        video.setId("videoId");
        when(videoRepository.findById(any())).thenReturn(java.util.Optional.of(video));
        when(s3Service.uploadFile(any())).thenThrow(new RuntimeException("Something wrong with S3"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            videoService.uploadThumbnail(mockMultipartFile, "videoId");
        });
        verify(videoRepository, times(1)).findById("videoId");
        verify(s3Service, times(1)).uploadFile(any());
        verify(videoRepository, times(0)).save(any());
        assertEquals("Something wrong with S3", exception.getMessage());
    }
}