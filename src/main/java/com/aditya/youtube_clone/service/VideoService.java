package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.model.Video;
import com.aditya.youtube_clone.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final S3Service s3Service;
    private final VideoRepository videoRepository;

    public void uploadVideo(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            log.error("❌Failed to upload video: File is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Uploaded file is empty");
        }
        log.info("🚀Uploading video file from service");
        // Upload file to AWS S3
        String videoUrl = s3Service.uploadFile(multipartFile);

        Video video = new Video();
        video.setVideoUrl(videoUrl);
        videoRepository.save(video);
        log.info("✅Video uploaded successfully");
    }
}
