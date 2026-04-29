package ru.ifmo.se.lab.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupRequestDto {
  @NotBlank
  @Size(min = 3, max = 20)
  private String login;

  @NotBlank
  @Size(max = 50)
  private String name;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;
}
