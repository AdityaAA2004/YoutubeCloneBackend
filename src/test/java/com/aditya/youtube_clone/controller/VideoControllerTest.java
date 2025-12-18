package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.config.TestSecurityConfig;
import com.aditya.youtube_clone.dto.VideoDTO;
import com.aditya.youtube_clone.dto.VideoUploadResponseDTO;
import com.aditya.youtube_clone.model.VideoStatus;
import com.aditya.youtube_clone.service.S3Service;
import com.aditya.youtube_clone.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class VideoControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    private MockMultipartFile mockMultipartFile;

    @MockBean
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        mockMultipartFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Dummy video content".getBytes()
        );

    }

    @Test
    @WithMockUser(username = "testuser")
    public void uploadVideoTest_Success() throws Exception {
        doReturn(new VideoUploadResponseDTO("1","https://example.com"))
                .when(videoService).uploadVideo(mockMultipartFile);

        mockMvc.perform(multipart("/api/videos")
                        .file(mockMultipartFile))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertNotNull(responseBody);
                    VideoUploadResponseDTO response = new ObjectMapper().readValue(responseBody, VideoUploadResponseDTO.class);
                    assertEquals("https://example.com", response.getVideoUrl());
                    assertEquals("1", response.getVideoId());
                });

        verify(videoService).uploadVideo(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void uploadVideoTest_IOError()  {

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "An I/O Exception occurred while uploading the file."))
                .when(videoService).uploadVideo(any());
        try {
            mockMvc.perform(multipart("/api/videos")
                            .file(mockMultipartFile))
                    .andExpect(status().is5xxServerError())
                    .andExpect(result -> {
                        // Get resolved exception
                        Exception resolved = result.getResolvedException();
                        // Ensure an exception was thrown
                        assertNotNull(resolved,
                                "No exception was thrown when one was expected.");
                        // Ensure exception type is ResponseStatusException
                        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,
                                () -> { throw resolved; },
                                "Expected a ResponseStatusException to be thrown.");
                        // Verify status code and message
                        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatusCode(),
                                "Expected HTTP status 500 INTERNAL_SERVER_ERROR.");
                        assertEquals("An I/O Exception occurred while uploading the file.",
                                responseStatusException.getReason(),
                                "Exception message does not match.");
                    });

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    public void uploadVideoTest_GenericError()  {
        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while uploading the file."))
                .when(videoService).uploadVideo(any());
        try {
            mockMvc.perform(multipart("/api/videos")
                            .file(mockMultipartFile))
                    .andExpect(status().is5xxServerError())
                    .andExpect(result -> {
                        // Get resolved exception
                        Exception resolved = result.getResolvedException();
                        // Ensure an exception was thrown
                        assertNotNull(resolved,
                                "No exception was thrown when one was expected.");
                        // Ensure exception type is ResponseStatusException
                        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,
                                () -> { throw resolved; },
                                "Expected a ResponseStatusException to be thrown.");
                        // Verify status code and message
                        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatusCode(),
                                "Expected HTTP status 500 INTERNAL_SERVER_ERROR.");
                        assertEquals("An unexpected error occurred while uploading the file.",
                                responseStatusException.getReason(),
                                "Exception message does not match.");
                    });

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    public void uploadVideoTest_EmptyFile() throws Exception {
        // Create an empty file
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty-video.mp4",
                "video/mp4",
                new byte[0]  // Empty byte array
        );

        doCallRealMethod().when(videoService).uploadVideo(any());

        // Perform the request with empty file
        mockMvc.perform(multipart("/api/videos")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");

                    assertInstanceOf(ResponseStatusException.class, resolved,
                            "Expected a ResponseStatusException to be thrown.");

                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;

                    assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatusCode(),
                            "Expected HTTP status 400 BAD_REQUEST.");
                    assertEquals("Uploaded file is empty",  // Match your actual service message
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });

        // Verify the service was called
        verify(videoService).uploadVideo(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void updateVideoMetadataTest_Success() throws Exception {
        // Create the request body
        JSONObject metadata = new JSONObject();
        metadata.put("id", "video123");
        metadata.put("title", "Updated Title");
        metadata.put("description", "Updated Description");
        metadata.put("thumbnailUrl", "https://thumbnail.url");
        metadata.put("videoStatus", "PUBLIC");
        metadata.put("videoUrl", "https://video.url");
        metadata.put("tags", new JSONArray(List.of("tag1", "tag2")));

        VideoDTO expectedResponse = new VideoDTO();
        expectedResponse.setId("video123");
        expectedResponse.setTitle("Updated Title");
        expectedResponse.setDescription("Updated Description");
        expectedResponse.setThumbnailUrl("https://thumbnail.url");
        expectedResponse.setVideoStatus(VideoStatus.valueOf("PUBLIC"));
        expectedResponse.setVideoUrl("https://video.url");
        expectedResponse.setTags(Set.of("tag1", "tag2"));

        // Mock the service
        when(videoService.editVideo(any(VideoDTO.class))).thenReturn(expectedResponse);

        // Perform PUT request
        mockMvc.perform(put("/api/videos")
                        .contentType("application/json")
                        .content(metadata.toString()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    assertEquals("video123", jsonResponse.getString("id"));
                    assertEquals("Updated Title", jsonResponse.getString("title"));
                    assertEquals("Updated Description", jsonResponse.getString("description"));
                    assertEquals("https://thumbnail.url", jsonResponse.getString("thumbnailUrl"));
                    assertEquals("PUBLIC", jsonResponse.getString("videoStatus"));
                    assertEquals("https://video.url", jsonResponse.getString("videoUrl"));

                    JSONArray tagsArray = jsonResponse.getJSONArray("tags");
                    assertEquals(2, tagsArray.length());
                    assertTrue(tagsArray.toString().contains("tag1"));
                    assertTrue(tagsArray.toString().contains("tag2"));
                });


        verify(videoService).editVideo(any(VideoDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void updateVideoMetadata_Test_VideoNotFound() throws Exception {
        // Create the request body
        JSONObject metadata = new JSONObject();
        metadata.put("id", "nonexistentVideo");
        metadata.put("title", "Updated Title");
        metadata.put("description", "Updated Description");
        metadata.put("thumbnailUrl", "https://thumbnail.url");
        metadata.put("videoStatus", "PUBLIC");
        metadata.put("videoUrl", "https://video.url");
        metadata.put("tags", new JSONArray(List.of("tag1", "tag2")));

        // Mock the service to throw exception
        when(videoService.editVideo(any(VideoDTO.class)))
                .thenThrow(new IllegalArgumentException("Cannot find video by ID: nonexistentVideo"));

        // Perform PUT request
        mockMvc.perform(put("/api/videos")
                        .contentType("application/json")
                        .content(metadata.toString()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");

                    assertInstanceOf(ResponseStatusException.class, resolved,
                            "Expected an ResponseStatusException to be thrown.");
                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;
                    assertEquals("Cannot find video by ID: nonexistentVideo",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });

        verify(videoService).editVideo(any(VideoDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void updateVideoMetadata_Test_GenericError() throws Exception {
        // Create the request body
        JSONObject metadata = new JSONObject();
        metadata.put("id", "video123");
        metadata.put("title", "Updated Title");
        metadata.put("description", "Updated Description");
        metadata.put("thumbnailUrl", "https://thumbnail.url");
        metadata.put("videoStatus", "PUBLIC");
        metadata.put("videoUrl", "https://video.url");
        metadata.put("tags", new JSONArray(List.of("tag1", "tag2")));

        // Mock the service to throw generic exception
        when(videoService.editVideo(any(VideoDTO.class)))
                .thenThrow(new RuntimeException("Database connection lost"));

        // Perform PUT request
        mockMvc.perform(put("/api/videos")
                        .contentType("application/json")
                        .content(metadata.toString()))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");

                    assertInstanceOf(RuntimeException.class, resolved,
                            "Expected a RuntimeException to be thrown.");
                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;
                    assertEquals("An unexpected error occurred: Database connection lost",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });

        verify(videoService).editVideo(any(VideoDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void healthCheckTest() throws Exception {
        // Perform GET request to /api/videos/health
        mockMvc.perform(get("/api/videos/health"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    assertEquals("OK", jsonResponse.getString("status"));
                    assertEquals("The Video API is healthy and operational.",
                            jsonResponse.getString("message"));
                });
    }

    @Test
    @WithMockUser(username = "testuser")
    public void uploadThumbnailTest_Success() throws Exception {
        String videoId = "video123";

        doReturn("https://thumbnail.url").when(videoService)
                .uploadThumbnail(any(), eq(videoId));

        // Perform the file upload request and verify the response status
        mockMvc.perform(multipart("/api/videos/thumbnail")
                        .file(mockMultipartFile)
                        .param("videoId", videoId))
                .andExpect(status().isCreated());
        verify(videoService).uploadThumbnail(any(), eq(videoId));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void uploadThumbnailTest_Failure() throws Exception {
        String videoId = "video123";

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while uploading the thumbnail."))
                .when(videoService).uploadThumbnail(any(), eq(videoId));

        // Perform the file upload request and verify the response status
        mockMvc.perform(multipart("/api/videos/thumbnail")
                        .file(mockMultipartFile)
                        .param("videoId", videoId))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");

                    assertInstanceOf(ResponseStatusException.class, resolved,
                            "Expected a ResponseStatusException to be thrown.");

                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;

                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatusException.getStatusCode(),
                            "Expected HTTP status 500 INTERNAL_SERVER_ERROR.");
                    assertEquals("An error occurred while uploading the thumbnail.",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });
        verify(videoService).uploadThumbnail(any(), eq(videoId));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void deleteVideoTest_Success() throws Exception {
        String videoId = "video123";

        doNothing().when(videoService).deleteVideoById(videoId);
        // Perform DELETE request
        mockMvc.perform(delete("/api/videos/{videoId}", videoId))
                .andExpect(status().isOk());

        verify(videoService).deleteVideoById(videoId);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void deleteVideoTest_VideoNotFound() throws Exception {
        String videoId = "nonexistentVideo";
        doThrow(new IllegalArgumentException("Cannot find video by ID: nonexistentVideo"))
                .when(videoService).deleteVideoById(videoId);
        // Perform DELETE request
        mockMvc.perform(delete("/api/videos/{videoId}", videoId))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");
                    assertInstanceOf(ResponseStatusException.class, resolved,
                            "Expected a ResponseStatusException to be thrown.");
                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;
                    assertEquals("Cannot find video by ID: nonexistentVideo",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });
        verify(videoService).deleteVideoById(videoId);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void deleteVideoTest_GenericError() throws Exception {
        String videoId = "video123";
        doThrow(new RuntimeException("Database connection lost"))
                .when(videoService).deleteVideoById(videoId);
        // Perform DELETE request
        mockMvc.perform(delete("/api/videos/{videoId}", videoId))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");
                    assertInstanceOf(RuntimeException.class, resolved,
                            "Expected a RuntimeException to be thrown.");
                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;
                    assertEquals("An unexpected error occurred: Database connection lost",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });
        verify(videoService).deleteVideoById(videoId);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void likeVideoTest_Success() throws Exception {
        String videoId = "video123";
        VideoDTO videoDTOResponse = new VideoDTO();
        videoDTOResponse.setId(videoId);
        videoDTOResponse.setTitle("title");
        videoDTOResponse.setDescription("description");
        videoDTOResponse.setVideoUrl("videoUrl");
        videoDTOResponse.setThumbnailUrl("thumbnailUrl");
        videoDTOResponse.setTags(ConcurrentHashMap.newKeySet());
        videoDTOResponse.setLikes(1);
        videoDTOResponse.setDislikes(0);
        videoDTOResponse.setVideoStatus(VideoStatus.PUBLIC);
        doReturn(videoDTOResponse).when(videoService).likeVideo("video123");
        mockMvc.perform(post("/api/videos/{videoId}/like", videoId))
                .andExpect(status().isOk()).andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    assertEquals("title", jsonResponse.getString("title"));
                    assertEquals("description", jsonResponse.getString("description"));
                    assertEquals("videoUrl", jsonResponse.getString("videoUrl"));
                    assertEquals("thumbnailUrl", jsonResponse.getString("thumbnailUrl"));
                    assertEquals(1, jsonResponse.getInt("likes"));
                    assertEquals(0, jsonResponse.getInt("dislikes"));
                    assertEquals(VideoStatus.PUBLIC.toString(), jsonResponse.get("videoStatus"));
                });
        verify(videoService, times(1)).likeVideo("video123");
    }

    @Test
    @WithMockUser(username = "testuser")
    public void likeVideoTest_VideoNotFound() throws Exception {
        String videoId = "nonexistentVideo";
        doThrow(new IllegalArgumentException("Cannot find video by ID: nonexistentVideo"))
                .when(videoService).likeVideo(videoId);
        mockMvc.perform(post("/api/videos/{videoId}/like", videoId))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");
                    assertInstanceOf(ResponseStatusException.class, resolved,
                            "Expected a ResponseStatusException to be thrown.");
                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;
                    assertEquals("Cannot find video by ID: nonexistentVideo",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });
        verify(videoService, times(1)).likeVideo(videoId);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void likeVideoTest_GenericError() throws Exception {
        String videoId = "video123";
        doThrow(new RuntimeException("Database connection lost"))
                .when(videoService).likeVideo(videoId);
        // Perform DELETE request
        mockMvc.perform(post("/api/videos/{videoId}/like", videoId))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> {
                    Exception resolved = result.getResolvedException();
                    assertNotNull(resolved,
                            "No exception was thrown when one was expected.");
                    assertInstanceOf(RuntimeException.class, resolved,
                            "Expected a RuntimeException to be thrown.");
                    ResponseStatusException responseStatusException = (ResponseStatusException) resolved;
                    assertEquals("An unexpected error occurred: Database connection lost",
                            responseStatusException.getReason(),
                            "Exception message does not match.");
                });
        verify(videoService, times(1)).likeVideo(videoId);
    }

}
