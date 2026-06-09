package org.example.week4.service;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.week4.domain.File;
import org.example.week4.domain.User;
import org.example.week4.dto.UserRegisterRequestDto;
import org.example.week4.dto.UserResponseDto;
import org.example.week4.dto.UserUpdateInfoRequestDto;
import org.example.week4.dto.UserUpdatePasswordRequestDto;
import org.example.week4.enums.FileCategory;
import org.example.week4.exception.BusinessException;
import org.example.week4.exception.NotFoundException;
import org.example.week4.repository.FileRepository;
import org.example.week4.repository.UserRepository;
import org.example.week4.utils.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;

    // 회원가입
    public UserResponseDto createUser(UserRegisterRequestDto request, MultipartFile file) {
        validateUniqueEmail(request.getEmail());
        validateUniqueNickname(request.getNickname(), null);

        User user = new User(
                null,
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );

        User savedUser = userRepository.save(user);

        File profileImage = fileService.storeFile(file, savedUser.getId(), FileCategory.PROFILE_IMAGE);
        savedUser.updateProfileImage(profileImage.getId());
        userRepository.save(savedUser);

        String profileImageUrl = FileUtils.toFullUrl(profileImage.getPath());

        return new UserResponseDto(savedUser, profileImageUrl);
    }

    // 회원 정보 조회
    public UserResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user_not_found"));
        String profileImageUrl = null;

        if (user.getFileId() != null) {
            File file = fileRepository.findById(user.getFileId()).orElseThrow(() -> new NotFoundException("file_not_found"));
            profileImageUrl = FileUtils.toFullUrl(file.getPath());
        }

        return new UserResponseDto(user, profileImageUrl);
    }

    // 회원 정보 수정
    public UserResponseDto updateUserInfo(Long userId, @Valid UserUpdateInfoRequestDto request, MultipartFile file) {

        boolean hasNickname = request.getNickname() != null && !request.getNickname().isBlank();
        boolean hasFile = file != null && !file.isEmpty();

        // 최소 1개 이상 필드 수정 필요
        if (!hasNickname && !hasFile) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "nickname_required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (hasNickname) {
            validateUniqueNickname(request.getNickname(), userId);
            user.updateNickname(request.getNickname());
        }

        if (hasFile) {
            File newProfileImage = fileService.storeFile(
                    file,
                    user.getId(),
                    FileCategory.PROFILE_IMAGE
            );

            user.updateProfileImage(newProfileImage.getId());
        }

        userRepository.save(user);

        String profileImageUrl = fileService.getFileUrl(user.getFileId());

        return new UserResponseDto(user, profileImageUrl);
    }

    // 비밀번호 수정
    public void updatePassword(@Positive Long userId, @Valid UserUpdatePasswordRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid_old_password");
        }

        user.updatePassword(request.getNewPassword());
        userRepository.save(user);
    }

    // 회원 탈퇴
    public void deleteUser(@Positive Long userId) {
        userRepository.deleteById(userId);
    }

    // 중복 이메일 검사
    private void validateUniqueEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "duplicate_email");
        }
    }

    // 중복 닉네임 검사
    private void validateUniqueNickname(String nickname, Long currentUserId) {
        userRepository.findByNickname(nickname)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "duplicate_nickname");
                });
    }
}
