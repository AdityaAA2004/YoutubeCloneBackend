package com.aditya.youtube_clone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Document(value = "Video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    private String id;
    private String title;
    private String description;
    private String userId;
    private AtomicInteger likes = new AtomicInteger(0);
    private AtomicInteger disLikes = new AtomicInteger(0);
    private Set<String> tags = new HashSet<>();
    private String videoUrl;
    private VideoStatus videoStatus = VideoStatus.PUBLIC; // or whatever default status you want
    private Integer viewCount = 0;
    private String thumbnailUrl;
    private List<Comment> comments = new ArrayList<>();

    public void incrementLikes() {
        likes.getAndIncrement();
    }

    public void decrementLikes() {
        likes.getAndDecrement();
    }

    public void decrementDisLikes() {
        disLikes.getAndDecrement();
    }
}