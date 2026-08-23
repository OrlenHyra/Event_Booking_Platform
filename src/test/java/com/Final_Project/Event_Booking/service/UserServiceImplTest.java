package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.InvalidRoleException;
import com.Final_Project.Event_Booking.exception.custom.UserNotFoundException;
import com.Final_Project.Event_Booking.exception.custom.UsernameAlreadyExistsException;
import com.Final_Project.Event_Booking.model.dto.request.UserRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.UserMapper;
import com.Final_Project.Event_Booking.repository.UserRepository;
import com.Final_Project.Event_Booking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDTO requestDTO;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test")
                .email("test@email.com")
                .password("encoded-testpassword")
                .role(UserRole.ATTENDEE)
                .active(true)
                .build();

        requestDTO = UserRequestDTO.builder()
                .username("test")
                .password("testpassword")
                .email("test@email.com")
                .role(UserRole.ATTENDEE)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMockedStatic != null) {
            securityContextHolderMockedStatic.close();
        }
    }

    private void mockAuthenticatedUsername(String username) {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        securityContextHolderMockedStatic
                .when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
    }

    @Test
    void createsUserSuccessfully() {
        User mappedUser = new User();
        mappedUser.setUsername("test");
        mappedUser.setEmail("test@email.com");

        UserResponseDTO expectedResponse =
                UserResponseDTO.builder()
                        .id(1L)
                        .username("test")
                        .build();

        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(userMapper.toEntity(requestDTO)).thenReturn(mappedUser);
        when(passwordEncoder.encode("testpassword"))
                .thenReturn("encoded-testpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);

        UserResponseDTO response = userService.createUser(requestDTO);

        assertThat(response).isEqualTo(expectedResponse);

        verify(userRepository).save(argThat(
                u -> u.getPassword().equals("encoded-testpassword")
        ));
    }

    @Test
    void cannotCreateUserWithExistingUsername() {
        when(userRepository.existsByUsername("test")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(requestDTO))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getsAllUsers() {
        UserResponseDTO responseDTO =
                UserResponseDTO.builder()
                        .id(1L)
                        .username("test")
                        .build();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        List<UserResponseDTO> responses = userService.getAllUsers();

        assertThat(responses).containsExactly(responseDTO);
    }

    @Test
    void updatesUserSuccessfully() {
        UserRequestDTO updateRequest =
                UserRequestDTO.builder()
                        .username("test")
                        .email("test@email.com")
                        .password("")
                        .role(UserRole.ATTENDEE)
                        .build();

        UserResponseDTO expectedResponse =
                UserResponseDTO.builder()
                        .id(1L)
                        .username("test")
                        .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);

        UserResponseDTO response =
                userService.updateUser(1L, updateRequest);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void cannotUpdateUserThatDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updateUser(99L, requestDTO))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updatesPasswordWhenProvided() {
        UserRequestDTO updateRequest =
                UserRequestDTO.builder()
                        .username("test")
                        .email("test@email.com")
                        .password("newtestpassword")
                        .role(UserRole.ATTENDEE)
                        .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newtestpassword"))
                .thenReturn("encoded-newtestpassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user))
                .thenReturn(UserResponseDTO.builder().id(1L).build());

        userService.updateUser(1L, updateRequest);

        verify(passwordEncoder).encode("newtestpassword");
    }

    @Test
    void deletesUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void cannotDeleteUserThatDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void updatesUserRole() {
        UserResponseDTO expectedResponse =
                UserResponseDTO.builder()
                        .id(1L)
                        .role(UserRole.ORGANIZER)
                        .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);

        UserResponseDTO response =
                userService.updateRole(1L, UserRole.ORGANIZER);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(user.getRole()).isEqualTo(UserRole.ORGANIZER);
    }

    @Test
    void cannotChangeRoleToAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.updateRole(1L, UserRole.ADMIN))
                .isInstanceOf(InvalidRoleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getsCurrentUser() {
        mockAuthenticatedUsername("test");

        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertThat(result).isEqualTo(user);
    }

    @Test
    void checksIfCurrentUserOwnsEvent() {
        mockAuthenticatedUsername("test");

        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        Event event = Event.builder()
                .id(1L)
                .organizer(user)
                .build();

        boolean result = userService.isCurrentUserOwner(event);

        assertThat(result).isTrue();
    }

    @Test
    void updatesActiveStatus() {
        UserResponseDTO expectedResponse =
                UserResponseDTO.builder()
                        .id(1L)
                        .active(false)
                        .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);

        UserResponseDTO response =
                userService.updateActiveStatus(1L, false);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(user.isActive()).isFalse();
    }
}