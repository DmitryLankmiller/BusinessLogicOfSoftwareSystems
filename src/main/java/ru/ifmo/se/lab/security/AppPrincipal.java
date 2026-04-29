package ru.ifmo.se.lab.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.ifmo.se.lab.model.AppRole;

@Getter
@AllArgsConstructor
@Builder
public class AppPrincipal {
    private final long id;
    private final String login;
    private final AppRole role;
}