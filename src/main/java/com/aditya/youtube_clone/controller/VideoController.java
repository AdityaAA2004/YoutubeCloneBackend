package com.aditya.youtube_clone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Appropriate response status for POST requests
    // (because generally it is used to create a new instance of entity)
    public void uploadVideo(@RequestParam("file") MultipartFile file) {

    }
}
