package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.week6.config.FileProperties;
import org.ktb.week6.entity.File;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.repository.FileRepository;
import org.ktb.week6.utils.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FileProperties fileProperties;

    // 파일 저장
    @Transactional
    public Optional<File> storeFile(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        FileUtils.validateImageFile(file);

        try {
            Path root = Path.of(fileProperties.uploadDir())
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
            return Optional.of(fileRepository.save(new File(filePath, category)));
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "file_upload_failed");
        }
    }

    public static <T> T withCleanupOnFailure(Optional<File> file, Consumer<File> cleanup, Supplier<T> action) {
        try {
            return action.get();
        } catch (RuntimeException e) {
            file.ifPresent(f -> {
                try {
                    cleanup.accept(f);
                } catch (RuntimeException cleanupException) {
                    log.error("Failed to clean up file {} after action failure", f.getId(), cleanupException);
                }
            });
            throw e;
        }
    }

    public void deleteFile(File file) {
        try {
            Path root = Path.of(fileProperties.uploadDir()).toAbsolutePath().normalize();
            String storeFileName = file.getPath().substring(file.getPath().lastIndexOf('/') + 1);
            Files.deleteIfExists(root.resolve(storeFileName).normalize());
        } catch (IOException ignored) {
        }
        fileRepository.delete(file);
    }
}
