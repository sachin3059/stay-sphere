package com.staysphere.property.service;

import com.staysphere.property.config.S3MediaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3MediaService {

    private static final Pattern SAFE_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3MediaProperties properties;

    public void ensureConfigured() {
        if (properties.getBucket() == null || properties.getBucket().isBlank()) {
            throw new IllegalStateException(
                    "AWS_S3_BUCKET is not configured for property media uploads");
        }
    }

    public String buildObjectKey(String propertyId, String fileName) {
        String safe = sanitizeFileName(fileName);
        return properties.getKeyPrefix() + "/" + propertyId + "/"
                + UUID.randomUUID() + "-" + safe;
    }

    public String createPresignedUploadUrl(String objectKey, String contentType) {
        ensureConfigured();
        validateContentType(contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(
                        Duration.ofMinutes(properties.getPresignExpiryMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    public void assertObjectExists(String objectKey) {
        ensureConfigured();
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build());
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException(
                    "Upload not found in storage for key: " + objectKey);
        } catch (S3Exception ex) {
            throw new IllegalStateException(
                    "Could not verify upload: " + ex.getMessage(), ex);
        }
    }

    public String publicUrlForKey(String objectKey) {
        String base = properties.getPublicBaseUrl();
        if (base != null && !base.isBlank()) {
            return base.replaceAll("/$", "") + "/" + objectKey;
        }
        return "https://" + properties.getBucket() + ".s3."
                + properties.getRegion() + ".amazonaws.com/" + objectKey;
    }

    public boolean isManagedMediaUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String base = properties.getPublicBaseUrl();
        if (base != null && !base.isBlank() && url.startsWith(base)) {
            return true;
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String bucketHost = properties.getBucket() + ".s3.";
            return host.contains(bucketHost)
                    || host.equals(properties.getBucket() + ".s3.amazonaws.com");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public void deleteByPublicUrl(String publicUrl) {
        if (!isManagedMediaUrl(publicUrl)) {
            throw new IllegalArgumentException("Not an S3 media URL for this bucket");
        }
        String key = extractObjectKey(publicUrl);
        ensureConfigured();
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build());
        log.info("Deleted S3 object: {}", key);
    }

    public String resolveContentType(String contentType, String fileName) {
        if (contentType != null && !contentType.isBlank()) {
            String primary = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
            if (!"application/octet-stream".equals(primary)) {
                validateContentType(primary);
                return primary;
            }
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("contentType or fileName is required");
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (lower.endsWith(".webm")) {
            return "video/webm";
        }
        if (lower.endsWith(".mov")) {
            return "video/quicktime";
        }
        throw new IllegalArgumentException(
                "Unsupported file type; set Content-Type or use a known extension");
    }

    public void validateFileSize(String contentType, long sizeBytes) {
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
        String type = normalizeContentType(contentType);
        if (ALLOWED_IMAGE_TYPES.contains(type)) {
            if (sizeBytes > properties.getMaxImageBytes()) {
                throw new IllegalArgumentException(
                        "Image exceeds max size of "
                                + properties.getMaxImageBytes() + " bytes");
            }
            return;
        }
        if (ALLOWED_VIDEO_TYPES.contains(type)) {
            if (sizeBytes > properties.getMaxVideoBytes()) {
                throw new IllegalArgumentException(
                        "Video exceeds max size of "
                                + properties.getMaxVideoBytes() + " bytes");
            }
            return;
        }
        validateContentType(contentType);
    }

    public void assertKeyBelongsToProperty(String objectKey, String propertyId) {
        String prefix = properties.getKeyPrefix() + "/" + propertyId + "/";
        if (objectKey == null || !objectKey.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid object key for this property");
        }
    }

    private String extractObjectKey(String publicUrl) {
        String base = properties.getPublicBaseUrl();
        if (base != null && !base.isBlank() && publicUrl.startsWith(base)) {
            return publicUrl.substring(base.replaceAll("/$", "").length() + 1);
        }
        URI uri = URI.create(publicUrl);
        String path = uri.getPath();
        if (path == null || path.length() < 2) {
            throw new IllegalArgumentException("Could not parse object key from URL");
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }
        String trimmed = fileName.trim();
        int slash = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
        if (slash >= 0 && slash < trimmed.length() - 1) {
            trimmed = trimmed.substring(slash + 1);
        }
        if (trimmed.length() > 120) {
            trimmed = trimmed.substring(trimmed.length() - 120);
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (!SAFE_NAME.matcher(lower).matches()) {
            int dot = lower.lastIndexOf('.');
            String ext = dot > 0 ? lower.substring(dot) : "";
            return "upload" + ext.replaceAll("[^a-z0-9.]", "");
        }
        return lower;
    }

    private static void validateContentType(String contentType) {
        String type = normalizeContentType(contentType);
        if (!ALLOWED_IMAGE_TYPES.contains(type)
                && !ALLOWED_VIDEO_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "Unsupported media type: " + contentType);
        }
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("contentType is required");
        }
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }
}
