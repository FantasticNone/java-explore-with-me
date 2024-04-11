package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exceptions.ConflictDataException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto newUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictDataException("Email must be unique.");
        }
        User user = userRepository.save(UserMapper.fromUserDto(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId));
        }

        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (from < 0 || size < 0) {
            throw new IllegalArgumentException("From and Size must be positive or zero");
        }

        List<User> users;

        if (ids != null) {
            users = userRepository.findAllById(ids);
        } else {
            users = userRepository.findAll(PageRequest.of(from / size, size)).getContent();
        }

        return UserMapper.toDto(users);
    }
}