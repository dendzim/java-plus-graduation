package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public UserDto adminAddNewUser(@NonNull NewUserRequest newUserRequest) {
		if (userRepository.existsByEmail(newUserRequest.email())) {
			throw new ConflictException("Пользователь с такой почтой " + newUserRequest.email() + " уже существует");
		}
		User user = UserMapper.toEntity(newUserRequest);
		return UserMapper.toUserDto(userRepository.save(user));
	}

	@Override
	public List<UserDto> getUsers(List<Long> ids, int from, int size) {
		PageRequest page = PageRequest.of(from / size, size);
		List<User> users;
		if (ids == null || ids.isEmpty()) {
			users = userRepository.findAll(page).getContent();
		} else {
			users = userRepository.findAllByIdIn(ids, page);
		}
		return users.stream()
				.map(UserMapper::toUserDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		checkUser(userId);
		userRepository.deleteById(userId);
	}

	@Override
	public void checkUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("Пользователь с id=" + userId + " не найден");
		}
	}

	@Override
	public List<UserDto> getUsersByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}

		Set<Long> uniqueIds = ids.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (uniqueIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<User> users = userRepository.findAllById(uniqueIds);

		return users.stream()
				.map(UserMapper::toUserDto)
				.collect(Collectors.toList());
	}

	@Override
	public UserDto findUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
		return UserMapper.toUserDto(user);
	}
}
