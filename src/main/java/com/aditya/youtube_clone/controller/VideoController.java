package com.aditya.youtube_clone.controller;

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
    public void uploadVideo(@RequestParam("file") MultipartFile file) {
        log.info("🚀Uploading video file from controller");
        videoService.uploadVideo(file);
    }
}
