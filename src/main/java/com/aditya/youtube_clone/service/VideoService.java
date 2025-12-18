package com.aditya.youtube_clone.service;

import com.aditya.youtube_clone.dto.VideoDTO;
import com.aditya.youtube_clone.dto.VideoUploadResponseDTO;
import com.aditya.youtube_clone.model.Video;
import com.aditya.youtube_clone.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final S3Service s3Service;
    private final UserService userService;
    private final VideoRepository videoRepository;

    public VideoUploadResponseDTO uploadVideo(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            log.error("❌Failed to upload video: File is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Uploaded file is empty");
        }
        log.info("🚀Uploading video file from service");
        // Upload file to AWS S3
        String videoUrl = s3Service.uploadFile(multipartFile);
        Video video = new Video();
        video.setVideoUrl(videoUrl);
        video.setLikes(new AtomicInteger(0));

        Video createdVideo = videoRepository.save(video);
        log.info("✅Video uploaded successfully");
        return new VideoUploadResponseDTO(createdVideo.getId(), createdVideo.getVideoUrl());
    }

    public VideoDTO editVideo(VideoDTO videoDTO) {
        // Find the video by videoID
        log.info("🚀Editing video metadata for video ID: {}", videoDTO.getId());
        log.info("🔎Finding video by ID: {}", videoDTO.getId());
        Video existingVideo = getVideoById(videoDTO.getId());
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

    public String uploadThumbnail(MultipartFile file, String videoId) {
        Video existingVideo = getVideoById(videoId);
        String thumbnailUrl = s3Service.uploadFile(file);
        existingVideo.setThumbnailUrl(thumbnailUrl);
        videoRepository.save(existingVideo);
        return thumbnailUrl;
    }

    Video getVideoById(String videoId) {
        return videoRepository.findById(videoId).orElseThrow(() ->
                new IllegalArgumentException("Cannot find video by ID: " + videoId)
        );
    }

    public void deleteVideoById(String videoId) {
        Video existingVideo = getVideoById(videoId);
        // check for thumbnail and video url and delete from s3
        if (existingVideo.getVideoUrl() != null) {
            s3Service.deleteFile(existingVideo.getVideoUrl());
        }
        if (existingVideo.getThumbnailUrl() != null) {
            s3Service.deleteFile(existingVideo.getThumbnailUrl());
        }
        videoRepository.delete(existingVideo);
        log.info("✅Video deleted successfully for video ID: {}", videoId);
    }

    public VideoDTO getVideoDetails(String videoId) {
        Video existingVideo = getVideoById(videoId);
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(existingVideo.getId());
        videoDTO.setTitle(existingVideo.getTitle());
        videoDTO.setDescription(existingVideo.getDescription());
        videoDTO.setTags(existingVideo.getTags());
        videoDTO.setVideoStatus(existingVideo.getVideoStatus());
        videoDTO.setVideoUrl(existingVideo.getVideoUrl());
        videoDTO.setThumbnailUrl(existingVideo.getThumbnailUrl());
        return videoDTO;
    }

    public VideoDTO likeVideo(String videoId) {
        Video existingVideo = getVideoById(videoId);
        // video like logic
        if (userService.checkUserLikedVideo(videoId)) {
            existingVideo.decrementLikes();
            userService.removeFromLikedVideos(videoId);
            log.info("🔽User removed like from video ID: {}", videoId);
        }
        else if (userService.checkUserDisLikedVideo(videoId)) {
            existingVideo.decrementDisLikes();
            userService.removeFromDisLikedVideos(videoId);
            existingVideo.incrementLikes();
            userService.addToLikedVideos(videoId);
            log.info("🔼User switched from dislike to like for video ID: {}", videoId);
        } else {
            existingVideo.incrementLikes();
            userService.addToLikedVideos(videoId);
            log.info("👍User liked video ID: {}", videoId);
        }
        videoRepository.save(existingVideo);
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(existingVideo.getId());
        videoDTO.setTitle(existingVideo.getTitle());
        videoDTO.setDescription(existingVideo.getDescription());
        videoDTO.setTags(existingVideo.getTags());
        videoDTO.setVideoStatus(existingVideo.getVideoStatus());
        videoDTO.setVideoUrl(existingVideo.getVideoUrl());
        videoDTO.setThumbnailUrl(existingVideo.getThumbnailUrl());
        videoDTO.setLikes(existingVideo.getLikes().get());
        videoDTO.setDislikes(existingVideo.getDisLikes().get());
        return videoDTO;
    }
}
