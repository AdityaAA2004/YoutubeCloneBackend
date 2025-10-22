package com.aditya.youtube_clone.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Test
    public void testUploadFile_Success() {
        // Create a mock MultipartFile
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        S3Utilities s3Utilities = mock(S3Utilities.class);
        URL mockURL = mock(URL.class);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(mockURL.toString()).thenReturn("http://mock-s3-url/test-file.txt");
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockURL);

        try {
            // Stub the methods of MultipartFile
            when(mockMultipartFile.getOriginalFilename()).thenReturn("test-file.txt");
            when(mockMultipartFile.getContentType()).thenReturn("text/plain");
            when(mockMultipartFile.getSize()).thenReturn(20L);
            when(mockMultipartFile.getInputStream()).thenReturn(
                    new java.io.ByteArrayInputStream("Dummy file content".getBytes())
            );

            // Call the method under test
            assertEquals("http://mock-s3-url/test-file.txt",s3Service.uploadFile(mockMultipartFile));

            // Verify that s3Client.putObject was called
            verify(s3Client, times(1)).putObject((PutObjectRequest) any(), (RequestBody) any());
        } catch (IOException e) {
            // This block should not be reached in this test
            log.error(e.getMessage());
        }
    }

    @Test
    public void testUploadFile_ThrowsException_OnIOError() throws IOException {
        // Create a mock MultipartFile instead of MockMultipartFile
        MultipartFile mockMultipartFile = mock(MultipartFile.class);

        // Now you can stub it
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException("I/O error"));

        // Assert that ResponseStatusException is thrown
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            s3Service.uploadFile(mockMultipartFile);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("An I/O Exception occurred while uploading the file.", exception.getReason());
    }

    @Test
    public void testUploadFile_ThrowsException_OnGenericError() throws IOException {
        // Create a mock MultipartFile instead of MockMultipartFile
        MultipartFile mockMultipartFile = mock(MultipartFile.class);

        // Now you can stub it
        when(mockMultipartFile.getInputStream()).thenThrow(new RuntimeException("Generic error"));

        // Assert that ResponseStatusException is thrown
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            s3Service.uploadFile(mockMultipartFile);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("An unknown error occurred while uploading the file.", exception.getReason());
    }
}