package com.staysphere.property.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "staysphere/" + folder,
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"));

            String url = (String) uploadResult.get("secure_url");
            log.info("Image uploaded to Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Cloudinary upload error: {}", e.getMessage());
            throw new RuntimeException(
                    "Image upload failed: " + e.getMessage());
        }
    }

    public List<String> uploadMultipleImages(
            List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadImage(file, folder));
        }
        return urls;
    }

    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Cloudinary delete error: {}", e.getMessage());
        }
    }

    public void deleteImageByUrl(String secureUrl) {
        deleteImage(extractPublicId(secureUrl));
    }

    static String extractPublicId(String secureUrl) {
        int uploadIdx = secureUrl.indexOf("/upload/");
        if (uploadIdx < 0) {
            throw new IllegalArgumentException("Not a Cloudinary image URL");
        }
        String path = secureUrl.substring(uploadIdx + "/upload/".length());
        if (path.startsWith("v") && path.contains("/")) {
            path = path.substring(path.indexOf('/') + 1);
        }
        int dot = path.lastIndexOf('.');
        if (dot > 0) {
            path = path.substring(0, dot);
        }
        return path;
    }
}