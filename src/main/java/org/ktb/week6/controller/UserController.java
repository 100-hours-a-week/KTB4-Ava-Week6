package org.ktb.week6.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.CustomUserDetails;
import org.ktb.week6.dto.UserRegisterRequestDto;
import org.ktb.week6.dto.UserResponseDto;
import org.ktb.week6.dto.UserUpdateInfoRequestDto;
import org.ktb.week6.dto.UserUpdatePasswordRequestDto;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute UserRegisterRequestDto request) {
        UserResponseDto result = userService.createUser(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_user_success", result));
    }

    // 회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDto result = userService.getUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.of(ApiResultStatus.SUCCESS, "user_retrieved_success", result));
    }

    // 회원 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute UserUpdateInfoRequestDto request) {
        UserResponseDto result = userService.updateUserInfo(userDetails.getId(), request, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "user_update_success", result));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdatePasswordRequestDto request
    ) {
        userService.updatePassword(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "password_update_success", null));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
