package ru.ifmo.se.lab.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.dto.UserDto;
import ru.ifmo.se.lab.security.SecurityUtils;
import ru.ifmo.se.lab.service.UserCrudService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserCrudService userCrudService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<UserDto> getUsers(
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return userCrudService.findUsers(SecurityUtils.getCurrentPrincipal(), page, size, sortBy, sortDir);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable @Positive int id) {
        return userCrudService.findUserById(SecurityUtils.getCurrentPrincipal(), id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        return userCrudService.addUser(SecurityUtils.getCurrentPrincipal(), userDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable @Positive int id,
            @RequestBody @Valid UserDto userDto) {
        return userCrudService.updateUser(SecurityUtils.getCurrentPrincipal(), id, userDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Positive int id) {
        userCrudService.deleteUser(SecurityUtils.getCurrentPrincipal(), id);
    }
}