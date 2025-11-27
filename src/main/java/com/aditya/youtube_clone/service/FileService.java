package com.aditya.youtube_clone.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public String uploadFile(MultipartFile file);

    public void deleteFile(String fileUrl);
}
