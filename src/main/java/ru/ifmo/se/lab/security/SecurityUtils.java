package ru.ifmo.se.lab.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.ifmo.se.lab.model.AppRole;

public class SecurityUtils {
    public static AppPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (UserDetailsImpl) authentication.getPrincipal();
        return AppPrincipal.builder()
                .id(principal.getId())
                .login(principal.getUsername())
                .role(AppRole.valueOf(principal.getAuthorities().stream().findFirst().get().getAuthority())).build();
    }
}