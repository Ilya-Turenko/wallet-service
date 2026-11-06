package ru.rest.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rest.wallet.dto.LoginRequest;
import ru.rest.wallet.dto.LoginResponse;
import ru.rest.wallet.entity.User;
import ru.rest.wallet.exception.InvalidCredentialsException;
import ru.rest.wallet.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void loginWithCorrectPasswordReturnsToken() {
        User user = new User();
        user.setId(3L);
        user.setEmail("test@example.com");
        user.setPasswordHash("stored-hash");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "CorrectPassword123!",
                "stored-hash"
        )).thenReturn(true);

        when(jwtService.createToken(3L))
                .thenReturn("test-token");

        LoginResponse response = userService.login(
                new LoginRequest(
                        "test@example.com",
                        "CorrectPassword123!"
                )
        );

        assertEquals("test-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());

        verify(jwtService).createToken(3L);
    }

    @Test
    void loginWithWrongPasswordThrowsException() {
        User user = new User();
        user.setId(3L);
        user.setPasswordHash("stored-hash");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword123!",
                "stored-hash"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        new LoginRequest(
                                "test@example.com",
                                "WrongPassword123!"
                        )
                )
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void loginWithUnknownEmailThrowsException() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(
                        new LoginRequest(
                                "unknown@example.com",
                                "Password123!"
                        )
                )
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }
}