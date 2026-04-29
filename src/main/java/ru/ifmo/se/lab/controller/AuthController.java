package ru.ifmo.se.lab.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.MessageResponseDto;
import ru.ifmo.se.lab.dto.auth.JwtResponseDto;
import ru.ifmo.se.lab.dto.auth.LoginRequestDto;
import ru.ifmo.se.lab.dto.auth.SignupRequestDto;
import ru.ifmo.se.lab.model.AppRole;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.JwtUtils;
import ru.ifmo.se.lab.security.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(JwtResponseDto.builder()
                .token(jwt)
                .id(userDetails.getId())
                .login(userDetails.getUsername())
                .role(roles.getFirst())
                .build());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDto signUpRequest,
            @RequestParam(required = false) String host) {
        if (userRepository.existsByLogin(signUpRequest.getLogin())) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: login is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: email is already in use!"));
        }

        boolean isHost = "true".equalsIgnoreCase(host) || host != null;

        User user = User.builder()
                .login(signUpRequest.getLogin())
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .role(isHost ? AppRole.ROLE_HOST : AppRole.ROLE_USER)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponseDto("User registered successfully!"));
    }
}
