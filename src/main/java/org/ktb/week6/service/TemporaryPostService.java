package org.ktb.week6.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.TemporaryPostRequestDto;
import org.ktb.week6.dto.TemporaryPostResponseDto;
import org.ktb.week6.entity.File;
import org.ktb.week6.entity.TemporaryPost;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.exception.NotFoundException;
import org.ktb.week6.repository.TemporaryPostRepository;
import org.ktb.week6.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemporaryPostService {

    private final TemporaryPostRepository temporaryPostRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    // 임시저장 조회
    @Transactional
    public TemporaryPostResponseDto getTemporaryPost(Long userId) {
        return temporaryPostRepository.findByUserId(userId)
                .map(TemporaryPostResponseDto::new)
                .orElse(null);
    }

    // 임시저장 생성/갱신
    @Transactional
    public TemporaryPostResponseDto createTemporaryPost(Long userId, @Valid TemporaryPostRequestDto request, MultipartFile image) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("user_not_found"));

        Optional<File> file = fileService.storeFile(image, FileCategory.POST_ATTACHMENT);

        TemporaryPost savedPost = temporaryPostRepository.findByUserId(userId)
                .map(post -> {
                    post.updateTitle(request.getTitle());
                    post.updateContent(request.getContent());
                    post.updateFile(file.orElse(null));
                    return post;
                })
                .orElseGet(() -> {
                    TemporaryPost newPost = new TemporaryPost(
                            request.getTitle(),
                            request.getContent(),
                            user,
                            file.orElse(null)
                    );

                    return temporaryPostRepository.save(newPost);
                });

        return new TemporaryPostResponseDto(savedPost);
    }

    // 임시저장 수정
    @Transactional
    public TemporaryPostResponseDto updateTemporaryPost(@Positive Long userId, @Positive Long temporaryId, @Valid TemporaryPostRequestDto request, MultipartFile image) {
        TemporaryPost post = temporaryPostRepository.findById(temporaryId).orElseThrow(() ->
                new NotFoundException("temporary_post_not_found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "temporary_post_update_forbidden");
        }

        Optional<File> file = fileService.storeFile(image, FileCategory.POST_ATTACHMENT);
        post.updateTitle(request.getTitle());
        post.updateContent(request.getContent());
        post.updateFile(file.orElse(null));

        return new TemporaryPostResponseDto(post);
    }

    // 임시저장 삭제
    @Transactional
    public void deleteTemporaryPost(Long temporaryPostId, Long userId) {
        TemporaryPost temporaryPost = temporaryPostRepository.findByIdAndUserId(temporaryPostId, userId)
                .orElseThrow(() -> new NotFoundException("temporary_post_not_found"));
        temporaryPostRepository.delete(temporaryPost);
    }
}
