package ru.rest.wallet.service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.rest.wallet.dto.CreateUserRequest;
import ru.rest.wallet.dto.UserResponse;
import ru.rest.wallet.entity.User;
import ru.rest.wallet.exception.UserAlreadyExistsException;
import ru.rest.wallet.repository.UserRepository;
import ru.rest.wallet.dto.LoginRequest;
import ru.rest.wallet.exception.InvalidCredentialsException;
import ru.rest.wallet.dto.LoginResponse;

import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        String password = request.password();

        if (password == null
                || password.isBlank()
                || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пароль должен содержать минимум 8 символов "
                            + "и занимать не более 72 байт в UTF-8"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());

        String passwordHash = passwordEncoder.encode(password);
        user.setPasswordHash(passwordHash);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        String passwordHash = user.getPasswordHash();

        if (passwordHash == null
                || !passwordEncoder.matches(
                request.password(),
                passwordHash
        )) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                jwtService.createToken(user.getId()),
                "Bearer"
        );
    }

}