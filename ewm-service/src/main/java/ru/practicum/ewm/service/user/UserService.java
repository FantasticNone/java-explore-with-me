package ru.practicum.ewm.service.user;

import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.request.NewRequest;

import java.util.List;

public interface UserService {

    UserDto newUser(NewRequest user);

    void deleteUser(long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
