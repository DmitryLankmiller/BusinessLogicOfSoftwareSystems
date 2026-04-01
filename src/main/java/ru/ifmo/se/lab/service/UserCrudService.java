package ru.ifmo.se.lab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.se.lab.dto.DtoMapper;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.dto.UserDto;
import ru.ifmo.se.lab.exception.ResourceNotFoundException;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.AppPrincipal;
import ru.ifmo.se.lab.security.AppRole;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCrudService {

    private final UserRepository userRepository;

    public PageResponse<UserDto> findUsers(AppPrincipal principal, int page, int size, String sortBy, String sortDir) {
        requireAdmin(principal);

        Sort sort = buildSort(sortBy, sortDir);
        Page<User> users = userRepository.findAll(PageRequest.of(page, size, sort));
        return buildUserPageResponse(users);
    }

    public UserDto findUserById(AppPrincipal principal, int id) {
        requireAdmin(principal);

        System.out.println("here1");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id=%d not found".formatted(id)));
        System.out.println("here2");
        return DtoMapper.toDto(user);
    }

    public UserDto addUser(AppPrincipal principal, UserDto userDto) {
        requireAdmin(principal);

        User user = DtoMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return DtoMapper.toDto(savedUser);
    }

    public UserDto updateUser(AppPrincipal principal, int id, UserDto userDto) {
        requireAdmin(principal);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id=%d not found".formatted(id)));

        user.setLogin(userDto.getLogin());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User savedUser = userRepository.save(user);
        return DtoMapper.toDto(savedUser);
    }

    public void deleteUser(AppPrincipal principal, int id) {
        requireAdmin(principal);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id=%d not found".formatted(id)));

        userRepository.delete(user);
    }

    private void requireAdmin(AppPrincipal principal) {
        if (principal.getRole() != AppRole.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private PageResponse<UserDto> buildUserPageResponse(Page<User> users) {
        List<UserDto> content = users.getContent().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageResponse<UserDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(users.getNumber());
        response.setSize(users.getSize());
        response.setTotalElements(users.getTotalElements());
        response.setTotalPages(users.getTotalPages());
        response.setHasNext(users.hasNext());
        return response;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        return sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }
}