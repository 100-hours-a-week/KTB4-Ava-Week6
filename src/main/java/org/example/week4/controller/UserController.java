package org.example.week4.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.week4.auth.JwtProvider;
import org.example.week4.dto.UserRegisterRequestDto;
import org.example.week4.dto.UserResponseDto;
import org.example.week4.dto.UserUpdateInfoRequestDto;
import org.example.week4.dto.UserUpdatePasswordRequestDto;
import org.example.week4.enums.ApiResultStatus;
import org.example.week4.response.ApiResponse;
import org.example.week4.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute UserRegisterRequestDto request) {
        UserResponseDto result = userService.createUser(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_user_success", result));
    }

    // 회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(HttpServletRequest httpServletRequest) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        UserResponseDto result = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.of(ApiResultStatus.SUCCESS, "user_retrieved_success", result));
    }

    // 회원 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(HttpServletRequest httpServletRequest, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute UserUpdateInfoRequestDto request) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        UserResponseDto result = userService.updateUserInfo(userId, request, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "user_update_success", result));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody UserUpdatePasswordRequestDto request
    ) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        userService.updatePassword(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "password_update_success", null));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteUser(HttpServletRequest httpServletRequest) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
