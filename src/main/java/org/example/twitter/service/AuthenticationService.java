package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.example.twitter.model.Role;
import org.example.twitter.model.User;
import org.example.twitter.repository.UserRepository;
import org.example.twitter.security.AuthenticationRequest;
import org.example.twitter.security.AuthenticationResponse;
import org.example.twitter.security.JwtService;
import org.example.twitter.security.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .build();
        user = userRepository.insert(user);
        var token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .status(HttpStatus.OK.name())
                .error(null)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));
        var token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .status(HttpStatus.OK.name())
                .error(null)
                .build();
    }
}
