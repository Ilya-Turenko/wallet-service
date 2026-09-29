package ru.rest.wallet.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.rest.wallet.dto.CreateUserRequest;
import ru.rest.wallet.dto.UserResponse;
import ru.rest.wallet.entity.User;
import ru.rest.wallet.exception.UserAlreadyExistsException;
import ru.rest.wallet.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {

            throw new UserAlreadyExistsException(
                    request.email()
            );
        }

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }

}
