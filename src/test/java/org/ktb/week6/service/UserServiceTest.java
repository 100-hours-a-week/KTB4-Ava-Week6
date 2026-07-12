package org.ktb.week6.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileService fileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MultipartFile file;

    private Long notFoundUserId;
    private Long userId;
    private String email;
    private String password;
    private String encodedPassword;
    private String nickname;
    private String newNickname;
    private File savedFile;
    private File newFile;
    private User savedUser;
    private UserRegisterRequestDto userRegisterRequestDto;
    private UserUpdateInfoRequestDto userUpdateInfoRequestDto;

    @BeforeEach
    void initData() {
        userId = 1L;
        notFoundUserId = 999L;

        email = "test@test.com";
        password = "Test1234!!";
        encodedPassword = "encodedPassword";
        nickname = "test";
        newNickname = "TestPassword12!";

        savedUser = new User(email, encodedPassword, nickname);
        ReflectionTestUtils.setField(savedUser, "id", userId);


        savedFile = new File(
                "/public/images/test.png",
                "http://localhost/public/images/test.png",
                FileCategory.PROFILE_IMAGE
        );
        newFile = new File(
                "/public/images/newTest.png",
                "http://localhost/public/images/newTest.png",
                FileCategory.PROFILE_IMAGE
        );

        userRegisterRequestDto = new UserRegisterRequestDto(email, password, nickname);
        userUpdateInfoRequestDto = new UserUpdateInfoRequestDto(newNickname);

    }

    @Test
    @DisplayName("프로필 이미지 없이 회원가입에 성공한다")
    void createUser_success_withoutProfileImage() {

        // Given
        // existsByEmail에 문자열이 들어오면 false(중복 아님)을 반환
        given(userRepository.existsByEmail(email)).willReturn(false);
        // existsByNickname에 문자열이 들어오면 false(중복 아님)을 반환
        given(userRepository.existsByNickname(nickname)).willReturn(false);
        given(fileService.storeFile(isNull(), eq(FileCategory.PROFILE_IMAGE)))
                .willReturn(Optional.empty()); // 프로필 파일이 없으면 빈 Optional 객체를 반환
        given(passwordEncoder.encode(password)).willReturn(encodedPassword); // 비밀번호 암호화 문자열 반환
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDto responseDto = userService.createUser(userRegisterRequestDto, null);

        // Then
        assertThat(responseDto.getEmail()).isEqualTo(email); // 저장된 이메일과 요청한 이메일이 일치하는지
        assertThat(responseDto.getNickname()).isEqualTo(nickname); // 저장된 닉네임과 요청한 닉네임 일치하는지
        assertThat(responseDto.getProfileImageUrl()).isNull(); // 저장된 파일을 null로 저장하는지

        verify(userRepository).existsByEmail(email); // email로 이메일 검증이 한 번 불려졌는지 (중복 검사)
        verify(userRepository).existsByNickname(nickname); // nickname으로 닉네임 검증이 한 번 불려졌는지 (중복 검사)
        verify(passwordEncoder).encode(password); // 비밀번호 암호화가 진행됐는지
        verify(userRepository).save(any(User.class)); // 회원 저장 메소드가 불렸는지
    }

    @Test
    @DisplayName("프로필 이미지와 함께 회원가입에 성공한다")
    void createUser_success_withProfileImage() {

        // Given
        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(false);
        given(fileService.storeFile(eq(file), eq(FileCategory.PROFILE_IMAGE)))
                .willReturn(Optional.of(savedFile)); // 파일 저장에 성공하면 저장된 파일 객체를 반환
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));


        // When
        UserResponseDto responseDto = userService.createUser(userRegisterRequestDto, file);

        // Then
        assertThat(responseDto.getEmail()).isEqualTo(email);
        assertThat(responseDto.getNickname()).isEqualTo(nickname);
        assertThat(responseDto.getProfileImageUrl())
                .isEqualTo(savedFile.getUrl()); // DTO의 profileImageUrl과 저장된 file의 getUrl이 동일한지

        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByNickname(nickname);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이메일이 중복되면 회원가입에 실패한다")
    void createUser_fail_duplicateEmail() {
        // Given
        given(userRepository.existsByEmail(email)).willReturn(true);

        // When + Then
        assertThatThrownBy(() -> userService.createUser(userRegisterRequestDto, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("email_duplicated");

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).existsByNickname(anyString()); // 닉네님 중복 검사 안 부르는지 확인
        // 중복 이메일로 회원 가입이 실패되면 파일 저장도 안 부르는지 확인
        verify(fileService, never()).storeFile(any(), any());
        verify(userRepository, never()).save(any(User.class)); // 회원 저장을 안 하는지 확인
    }

    @Test
    @DisplayName("닉네임이 중복되면 회원가입에 실패한다")
    void createUser_fail_duplicateNickname() {
        // Given
        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(true);

        // When + Then
        assertThatThrownBy(() -> userService.createUser(userRegisterRequestDto, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("nickname_duplicated");

        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByNickname(nickname);
        // 중복 닉네임으로 회원 가입이 실패되면 파일 저장도 안 부르는지 확인
        verify(fileService, never()).storeFile(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("존재하는 유저 ID로 유저 조회에 성공한다")
    void getUser_success() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When
        UserResponseDto userResponseDto = userService.getUser(userId);

        // Then
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getId()).isEqualTo(userId); // 응답 DTO의 id가 요청한 userId와 일치한지
        assertThat(userResponseDto.getNickname())
                .isEqualTo(savedUser.getNickname()); // 응답 DTO의 닉네임이 요청한 회원의 닉네임과 같은지
    }


    @Test
    @DisplayName("존재하지 않는 유저 ID로 유저 조회에 실패한다")
    void getUser_fail() {
        // Given
        given(userRepository.findById(notFoundUserId)).willReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> userService.getUser(notFoundUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user_not_found"); // 회원 없음 예외가 발생하는지
    }

    @Test
    @DisplayName("탈퇴한 유저이면 유저 조회에 실패한다")
    void getUser_fail_deletedUser() {
        // Given
        savedUser.deleteUser();
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When + Then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user_not_found");
    }

    @Test
    @DisplayName("모든 필드를 성공적으로 수정한다")
    void updateUserInfo_success() {
        // Given
        UserUpdateInfoRequestDto requestDto = new UserUpdateInfoRequestDto(newNickname);

        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));
        given(fileService.storeFile(file, FileCategory.PROFILE_IMAGE)).willReturn(Optional.of(newFile));

        // When
        UserResponseDto responseDto = userService.updateUserInfo(userId, requestDto, file);

        // Then
        assertThat(responseDto.getNickname()).isEqualTo(newNickname);
        assertThat(responseDto.getProfileImageUrl()).isEqualTo(newFile.getUrl());

        verify(userRepository).existsByNickname(newNickname);

    }

    @Test
    @DisplayName("닉네임만 수정된다")
    void updateUserInfo_success_nickname() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When
        UserResponseDto responseDto = userService.updateUserInfo(userId, userUpdateInfoRequestDto, null);

        // Then
        assertThat(responseDto.getNickname()).isEqualTo(newNickname);
        assertThat(responseDto.getProfileImageUrl()).isNull();

        verify(userRepository).existsByNickname(newNickname);
        verify(fileService, never()).storeFile(any(), any()); // 파일 저장 안 불렸는지 확인
    }

    @Test
    @DisplayName("파일만 변경되면 파일만 수정에 성공한다")
    void updateUserInfo_success_file() {
        // Given
        UserUpdateInfoRequestDto requestDto = new UserUpdateInfoRequestDto(null);
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));
        given(fileService.storeFile(eq(file), eq(FileCategory.PROFILE_IMAGE)))
                .willReturn(Optional.of(newFile));

        // When
        UserResponseDto responseDto = userService.updateUserInfo(userId, requestDto, file);

        // Then
        assertThat(responseDto.getProfileImageUrl()).isEqualTo(newFile.getUrl());
        assertThat(responseDto.getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("회원 정보 수정에서 존재하지 않는 유저 ID로 요청을 하면 예외가 발생한다.")
    void updateUserInfo_fail_notFoundUser() {
        // Given
        given(userRepository.findById(notFoundUserId)).willReturn(Optional.empty());

        // When

        // Then
        assertThatThrownBy(() -> userService.updateUserInfo(notFoundUserId, userUpdateInfoRequestDto, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user_not_found");

    }

    @Test
    @DisplayName("회원 정보 수정에서 탈퇴한 유저이면 실패한다")
    void updateUserInfo_fail_deletedUser() {
        // Given
        savedUser.deleteUser();
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When + Then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, userUpdateInfoRequestDto, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user_not_found");

        verify(userRepository, never()).existsByNickname(anyString());
        verify(fileService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("모든 필드가 누락되면 예외가 발생한다")
    void updateUserInfo_fail_allFieldsMissing() {
        // Given
        UserUpdateInfoRequestDto requestDto = new UserUpdateInfoRequestDto(null);
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When + Then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, requestDto, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("no_update_fields");
        verify(fileService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("회원 정보 수정에서 닉네임이 중복되면 실패한다")
    void updateUserInfo_fail_duplicateNickname() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));
        given(userRepository.existsByNickname(newNickname)).willReturn(true);

        // When + Then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, userUpdateInfoRequestDto, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("nickname_duplicated");
    }

    @Test
    @DisplayName("비밀번호 변경에 성공한다")
    void updatePassword() {
        // Given
        String newPassword = "tesTpassworD12!";
        String encodedNewPassword = "encodedNewPassword";
        UserUpdatePasswordRequestDto requestDto = new UserUpdatePasswordRequestDto(password, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(requestDto.getOldPassword(), savedUser.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);


        // When
        userService.updatePassword(userId, requestDto);

        // Then
        assertThat(savedUser.getPassword()).isEqualTo(encodedNewPassword);
        assertThat(savedUser.getPassword()).isNotEqualTo(encodedPassword);

        verify(passwordEncoder).matches(requestDto.getOldPassword(), encodedPassword);
        verify(passwordEncoder).encode(newPassword);

    }

    @Test
    @DisplayName("Old Password가 일치하지 않으면 비밀번호 변경에 실패한다")
    void updatePassword_fail_oldPasswordMismatch() {
        // Given
        String wrongPassword = "wrongPassword";
        String newPassword = "tesTpassworD12!";
        UserUpdatePasswordRequestDto requestDto = new UserUpdatePasswordRequestDto(wrongPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(wrongPassword, encodedPassword)).willReturn(false);

        // When + Then
        assertThatThrownBy(() -> userService.updatePassword(userId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("invalid_old_password");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("회원 삭제에 성공한다")
    void deleteUser_success() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When
        userService.deleteUser(userId);

        // Then
        assertThat(savedUser.getStatus()).isEqualTo(StatusType.DELETED);
        assertThat(savedUser.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 USER ID로 회원 삭제에 실패한다")
    void deleteUser_fail() {
        // Given
        given(userRepository.findById(notFoundUserId)).willReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> userService.deleteUser(notFoundUserId))
                .isInstanceOf(NotFoundException.class);

    }

    @Test
    @DisplayName("탈퇴한 유저이면 회원 삭제에 실패한다")
    void deleteUser_fail_deletedUser() {
        // Given
        savedUser.deleteUser();
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When + Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user_not_found");
    }
}
