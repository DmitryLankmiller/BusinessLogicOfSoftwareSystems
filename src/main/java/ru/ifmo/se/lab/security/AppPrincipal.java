package ru.ifmo.se.lab.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppPrincipal {
    private final int id;
    private final String login;
    private final AppRole role;
}