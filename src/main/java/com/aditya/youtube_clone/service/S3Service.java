package com.aditya.youtube_clone.service;

import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import static com.aditya.youtube_clone.constants.Constants.*;
import java.io.IOException;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service implements FileService {

    private final S3Client s3Client;

    @Override
    public String uploadFile(MultipartFile file) {
        log.info("🚀Uploading file to S3 bucket");
        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileUuid = UUID.randomUUID().toString() + "." + fileExtension;
        ObjectMetadata fileMetadata = new ObjectMetadata.Builder().
                contentType(file.getContentType()).
                contentLength(file.getSize()).build();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(AWS_S3_BUCKET_NAME)
                    .key(fileUuid).metadata(fileMetadata.getMetadata())
                    .build();
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("✅Successfully uploaded file to S3 bucket");
        } catch (IOException ioException) {
            log.info("❌Failed to upload file to S3 bucket due to I/O Exception");
            log.error(ioException.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An I/O Exception occurred while uploading the file.");
        }
        catch (Exception exception) {
            log.error("❌Failed to upload file to S3 bucket due to unknown exception");
            log.error(exception.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unknown error occurred while uploading the file.");
        }
        PutObjectAclRequest objectAclRequest = PutObjectAclRequest.builder()
                .bucket(AWS_S3_BUCKET_NAME)
                .key(fileUuid).acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        s3Client.putObjectAcl(objectAclRequest);
        return s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(AWS_S3_BUCKET_NAME).key(fileUuid)
                .build()).toString();
    }

    @Override
    public void deleteFile(String fileUrl) {
        log.info("🚀Deleting file from S3 bucket");
        String fileKey = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        try {
            s3Client.deleteObject(builder -> builder.bucket(AWS_S3_BUCKET_NAME).key(fileKey).build());
            log.info("✅Successfully deleted file from S3 bucket");
        } catch (Exception exception) {
            log.error("❌Failed to delete file from S3 bucket: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while deleting the file.");
        }
    }
}
