package com.aditya.youtube_clone.controller;

import com.aditya.youtube_clone.dto.VideoDTO;
import com.aditya.youtube_clone.dto.VideoUploadResponseDTO;
import com.aditya.youtube_clone.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return videoService.editVideo(videoDTO);
    }
}
