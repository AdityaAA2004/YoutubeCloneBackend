package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.dto.VideoDTO;
import com.aditya.youtube_clone.dto.VideoUploadResponseDTO;
import com.aditya.youtube_clone.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Appropriate response status for POST requests
    // (because generally it is used to create a new instance of entity)
    public ResponseEntity<VideoUploadResponseDTO> uploadVideo(@RequestParam("file") MultipartFile file) {
        log.info("🚀Uploading video file from controller");
        VideoUploadResponseDTO uploadResponse = videoService.uploadVideo(file);
        String videoUrl = uploadResponse.getVideoUrl();
        try {
            return ResponseEntity.created(new URI(videoUrl)).body(
                    uploadResponse
            );
        } catch (URISyntaxException e) {
            log.error("❌Error creating URI for video upload: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating URI: " + e.getMessage());
        }
    }

    @PostMapping("/thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<JSONObject> uploadThumbnail(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("videoId") String videoId) {
        log.info("🚀Uploading thumbnail file from controller");
        String thumbnailUrl = videoService.uploadThumbnail(file, videoId);
        try {
            JSONObject responseObj = new JSONObject();
            responseObj.put("thumbnailUrl", thumbnailUrl);
            return ResponseEntity.created(new URI(thumbnailUrl)).
                    contentType(MediaType.APPLICATION_JSON).
                    body(responseObj);
        } catch (JSONException e) {
            log.error("❌Error creating JSON response for thumbnail upload: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating response JSON: " + e.getMessage());
        } catch (URISyntaxException e) {
            log.error("❌Error creating URI for thumbnail upload: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating URI: " + e.getMessage());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<VideoDTO> updateVideoMetadata(@RequestBody VideoDTO videoDTO) {
        log.info("🚀Updating video file from controller");
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(videoService.editVideo(videoDTO));
        } catch (IllegalArgumentException e) {
            log.error("❌Error updating video metadata: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("❌Unexpected error updating video metadata: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<JSONObject> health() throws JSONException {
        log.info("✅ Health check endpoint called");
        try {
            JSONObject healthResponse = new JSONObject();
            healthResponse.put("status", "OK");
            healthResponse.put("message", "The Video API is healthy and operational.");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(healthResponse);
        } catch (JSONException e) {
            log.error("❌Error creating JSON response for health check: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating response JSON: " + e.getMessage());
        }
    }
}
