package ru.ifmo.se.lab.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponseDto {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String login;
    private String role;
}
