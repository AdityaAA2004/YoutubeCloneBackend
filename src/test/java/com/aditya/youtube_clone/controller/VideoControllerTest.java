package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.service.S3Service;
import com.aditya.youtube_clone.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class VideoControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoService videoService;

    private MockMultipartFile mockMultipartFile;

    @MockitoBean
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        mockMultipartFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Dummy video content".getBytes()
        );

        doNothing().when(videoService).uploadVideo(any());
    }

    @Test
    public void uploadVideoTest_Success() throws Exception {

        doNothing().when(videoService).uploadVideo(any());
        // Perform the file upload request and verify the response status
        mockMvc.perform(multipart("/api/videos")
                .file(mockMultipartFile))
                .andExpect(status().isCreated());
        verify(videoService).uploadVideo(any());
    }

    @Test
    public void uploadVideoTest_IOError() throws IOException {

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

}
