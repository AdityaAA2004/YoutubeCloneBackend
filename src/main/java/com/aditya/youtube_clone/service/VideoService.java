package com.aditya.youtube_clone.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

    public void uploadVideo(MultipartFile multipartFile) {
        // Upload file to AWS S3
        // Save video data to Database
    }
}
