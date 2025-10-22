package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.dto.VideoDTO;
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

    public String uploadVideo(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            log.error("❌Failed to upload video: File is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Uploaded file is empty");
        }
        log.info("🚀Uploading video file from service");
        // Upload file to AWS S3
        String videoUrl = s3Service.uploadFile(multipartFile);

        Video video = new Video();
        video.setVideoUrl(videoUrl);
        Video createdVideo = videoRepository.save(video);
        log.info("✅Video uploaded successfully");
        return createdVideo.getId();
    }

    public VideoDTO editVideo(VideoDTO videoDTO) {
        // Find the video by videoID
        log.info("🚀Editing video metadata for video ID: {}", videoDTO.getId());
        log.info("🔎Finding video by ID: {}", videoDTO.getId());
        Video existingVideo = videoRepository.findById(videoDTO.getId()).orElseThrow(() ->
                new IllegalArgumentException("Cannot find video by ID: " + videoDTO.getId())
        );
        // Map videoDTO fields to video entity
        log.info("🚀Mapping VideoDTO fields to existing Video entity");
        existingVideo.setTitle(videoDTO.getTitle());
        existingVideo.setDescription(videoDTO.getDescription());
        existingVideo.setTags(videoDTO.getTags());
        existingVideo.setVideoStatus(videoDTO.getVideoStatus());
        existingVideo.setThumbnailUrl(videoDTO.getThumbnailUrl());
        // Save the updated video entity
        videoRepository.save(existingVideo);
        log.info("✅Video metadata updated successfully for video ID: {}", videoDTO.getId());
        return videoDTO;
    }
}
