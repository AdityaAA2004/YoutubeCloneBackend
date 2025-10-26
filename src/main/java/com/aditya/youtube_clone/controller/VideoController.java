package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.dto.VideoDTO;
import com.aditya.youtube_clone.dto.VideoUploadResponseDTO;
import com.aditya.youtube_clone.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Appropriate response status for POST requests
    // (because generally it is used to create a new instance of entity)
    public VideoUploadResponseDTO uploadVideo(@RequestParam("file") MultipartFile file) {
        log.info("🚀Uploading video file from controller");
        return new VideoUploadResponseDTO(videoService.uploadVideo(file));
    }

    @PostMapping("/thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadThumbnail(@RequestParam("file") MultipartFile file,
                                  @RequestParam("videoId") String videoId) {
        log.info("🚀Uploading thumbnail file from controller");
        return "";
        // return videoService.uploadThumbnail(videoId, file);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public VideoDTO updateVideoMetadata(@RequestBody VideoDTO videoDTO) {
        log.info("🚀Updating video file from controller");
        try {
            return videoService.editVideo(videoDTO);
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
    public JSONObject health() throws JSONException {
        JSONObject healthResponse = new JSONObject();
        healthResponse.put("status", "OK");
        healthResponse.put("message", "The Video API is healthy and operational.");
        return healthResponse;
    }
}
