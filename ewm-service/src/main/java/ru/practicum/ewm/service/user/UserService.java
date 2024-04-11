package ru.practicum.ewm.service.user;

import ru.practicum.ewm.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto newUser(UserDto user);

    void deleteUser(long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
