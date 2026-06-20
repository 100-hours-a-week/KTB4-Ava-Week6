package org.ktb.week6.utils;

import org.ktb.week6.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

public class FileUtils {
    // 허용할 확장자(이미지)
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    private FileUtils() {
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");
    }

    // 허용하는 확장자인지 검증
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "file_empty");
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(fileName);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_extension");
        }
    }

    // 상대 경로에 서버 도메인 붙여 전체 URL 생성
    public static String toFullUrl(String path) {
        if (path == null || path.isBlank()) return null;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        return baseUrl + path;
    }

    // 저장용 파일명 생성
    public static String createStoreFileName(String fileName) {
        String extension = getExtension(fileName);

        if (extension == null || extension.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_extension");
        }

        return UUID.randomUUID().toString() + "." + extension;
    }

    // 확장자 추출
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
