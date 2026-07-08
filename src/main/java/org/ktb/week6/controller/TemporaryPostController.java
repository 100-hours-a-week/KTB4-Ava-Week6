package org.ktb.week6.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.CustomUserDetails;
import org.ktb.week6.dto.TemporaryPostRequestDto;
import org.ktb.week6.dto.TemporaryPostResponseDto;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.TemporaryPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/temporary-posts")
@RequiredArgsConstructor
public class TemporaryPostController {

    private final TemporaryPostService temporaryPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<TemporaryPostResponseDto>> getTemporaryPost(@AuthenticationPrincipal CustomUserDetails userDetails) {
        TemporaryPostResponseDto result = temporaryPostService.getTemporaryPost(userDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "temporary_post_retrieved_success", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemporaryPostResponseDto>> createTemporaryPost(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute TemporaryPostRequestDto request) {
        TemporaryPostResponseDto result = temporaryPostService.createTemporaryPost(userDetails.getId(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_temporary_post_success", result));
    }

    @PatchMapping("/{temporaryId}")
    public ResponseEntity<ApiResponse<TemporaryPostResponseDto>> updateTemporaryPost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("temporaryId") Long temporaryPostId, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute TemporaryPostRequestDto request) {
        TemporaryPostResponseDto result = temporaryPostService.updateTemporaryPost(userDetails.getId(), temporaryPostId, request, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "temporary_post_updated_success", result));
    }
}
