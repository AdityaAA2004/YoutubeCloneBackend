package com.aditya.youtube_clone.repository;

import com.aditya.youtube_clone.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoRepository extends MongoRepository<Video, String> {
}
