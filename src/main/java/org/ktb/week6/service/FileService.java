package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.entity.File;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.FileRepository;
import org.ktb.week6.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final String DEFAULT_PROFILE_IMAGE_PATH =
            "/public/images/defaultProfileImage.png";

    private final FileRepository fileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 저장
    public File storeFile(MultipartFile file, Long uploaderId, FileCategory category) {
        if (file == null || file.isEmpty()) {
            return handleEmptyFile(uploaderId, category);
        }

        FileUtils.validateImageFile(file);

        try {
            Path root = Path.of(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(root);

            String storeFileName = FileUtils.createStoreFileName(file.getOriginalFilename());

            Path path = root.resolve(storeFileName).normalize();

            if (!path.startsWith(root)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_file_path");
            }

            file.transferTo(path);

            String filePath = "/public/images/" + storeFileName;
            String fileUrl = getFileUrl(filePath);
            return fileRepository.save(new File(filePath, fileUrl, category));
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "file_upload_failed");
        }
    }

    // 파일 없을 때 처리
    private File handleEmptyFile(Long uploaderId, FileCategory category) {
        if (category == FileCategory.PROFILE_IMAGE) {
            return fileRepository.save(new File(
                    DEFAULT_PROFILE_IMAGE_PATH,
                    null,
                    category
            ));
        }

        // 게시글은 없어도 됨
        if (category == FileCategory.POST_ATTACHMENT) {
            return null;
        }

        throw new BusinessException(HttpStatus.BAD_REQUEST, "file_empty");
    }

    // 파일 URL 변환 (파일 ID 기반)
    public String getFileUrl(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file_not_found"));

        return FileUtils.toFullUrl(file.getPath());
    }

    // 파일 URL 변환 (파일 경로 기반)
    public String getFileUrl(String path) {
        return FileUtils.toFullUrl(path);
    }

    // 파일 삭제
    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }
}
