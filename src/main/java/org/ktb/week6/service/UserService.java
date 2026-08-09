package org.ktb.week6.service;


import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.UserRegisterRequestDto;
import org.ktb.week6.dto.UserResponseDto;
import org.ktb.week6.dto.UserUpdateInfoRequestDto;
import org.ktb.week6.dto.UserUpdatePasswordRequestDto;
import org.ktb.week6.entity.File;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    // 회원 생성
    @Transactional
    public UserResponseDto createUser(UserRegisterRequestDto request, MultipartFile file) {
        if (!validateUniqueEmail(request.getEmail()))
            throw new BusinessException(HttpStatus.CONFLICT, "email_duplicated");
        if (!validateUniqueNickname(request.getNickname()))
            throw new BusinessException(HttpStatus.CONFLICT, "nickname_duplicated");

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        User savedUser = userRepository.save(user);

        Optional<File> newFile = fileService.storeFile(file, FileCategory.PROFILE_IMAGE);
        return FileService.withCleanupOnFailure(newFile, fileService::deleteFile, () -> {
            savedUser.updateFile(newFile.orElse(null));
            return new UserResponseDto(savedUser);
        });
    }

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId) {
        User user = findActiveUser(userId);
        return new UserResponseDto(user);
    }

    // 회원 정보 수정
    @Transactional
    public UserResponseDto updateUserInfo(Long userId, UserUpdateInfoRequestDto request, MultipartFile file) {

        User user = findActiveUser(userId);

        boolean hasNickname = request.getNickname() != null && !request.getNickname().isBlank();
        boolean hasFile = file != null && !file.isEmpty();

        // 최소 1개 이상 필드 수정 필요
        if (!hasNickname && !hasFile) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "no_update_fields");
        }

        if (hasNickname) {
            if (!validateUniqueNickname(request.getNickname()))
                throw new BusinessException(HttpStatus.CONFLICT, "nickname_duplicated");
            user.updateNickname(request.getNickname());
        }

        Optional<File> newFile = hasFile
                ? fileService.storeFile(file, FileCategory.PROFILE_IMAGE)
                : Optional.empty();

        return FileService.withCleanupOnFailure(newFile, fileService::deleteFile, () -> {
            if (hasFile) {
                user.updateFile(newFile.orElse(null));
            }

            return new UserResponseDto(user);
        });
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(Long userId, UserUpdatePasswordRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_old_password");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        user.updatePassword(encodedNewPassword);
    }

    // 회원 삭제 (soft delete)
    @Transactional
    public void deleteUser(Long userId, String reason) {
        User user = findActiveUser(userId);

        user.insertDeletedReason(reason);
        user.deleteUser(); // soft delete 처리
        user.insertDeletedAt(LocalDateTime.now());
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (user.getStatus() == StatusType.DELETED) {
            throw new NotFoundException("user_not_found");
        }

        return user;
    }

    // 중복 이메일 검사
    private Boolean validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email))
            return false;
        return true;
    }

    // 중복 닉네임 검사
    private Boolean validateUniqueNickname(String nickname) {
        if (userRepository.existsByNickname(nickname))
            return false;
        return true;
    }
}
