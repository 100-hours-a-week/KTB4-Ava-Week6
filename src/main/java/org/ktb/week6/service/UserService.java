package org.ktb.week6.service;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.UserRegisterRequestDto;
import org.ktb.week6.dto.UserResponseDto;
import org.ktb.week6.dto.UserUpdateInfoRequestDto;
import org.ktb.week6.dto.UserUpdatePasswordRequestDto;
import org.ktb.week6.entity.File;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.FileRepository;
import org.ktb.week6.repository.UserRepository;
import org.springframework.http.HttpStatus;
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
    private final FileRepository fileRepository;

    // 회원가입
    @Transactional
    public UserResponseDto createUser(UserRegisterRequestDto request, MultipartFile file) {
        validateUniqueEmail(request.getEmail());
        validateUniqueNickname(request.getNickname());

        User user = new User(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );

        User savedUser = userRepository.save(user);

        Optional<File> newFile = fileService.storeFile(file, FileCategory.PROFILE_IMAGE);
        savedUser.updateFile(newFile.orElse(null));

        return new UserResponseDto(savedUser);
    }

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));
        return new UserResponseDto(user);
    }

    // 회원 정보 수정
    @Transactional
    public UserResponseDto updateUserInfo(Long userId, @Valid UserUpdateInfoRequestDto request, MultipartFile file) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        boolean hasNickname = request.getNickname() != null && !request.getNickname().isBlank();
        boolean hasFile = file != null && !file.isEmpty();

        // 최소 1개 이상 필드 수정 필요
        if (!hasNickname && !hasFile) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "no_update_fields");
        }

        if (hasNickname) {
            validateUniqueNickname(request.getNickname());
            user.updateNickname(request.getNickname());
        }

        if (hasFile) {
            Optional<File> newFile = fileService.storeFile(
                    file,
                    FileCategory.PROFILE_IMAGE
            );
            user.updateFile(newFile.get());
        }

        return new UserResponseDto(user);
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(@Positive Long userId, @Valid UserUpdatePasswordRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_old_password");
        }

        user.updatePassword(request.getNewPassword());
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(@Positive Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        user.deleteUser(); // soft delete 처리
        user.setDeletedAt(LocalDateTime.now());
    }

    // 중복 이메일 검사
    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "email_duplicated");
        }
    }

    // 중복 닉네임 검사
    private void validateUniqueNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(HttpStatus.CONFLICT, "nickname_duplicated");
        }
    }
}
