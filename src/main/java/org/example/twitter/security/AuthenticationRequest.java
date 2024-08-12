package org.example.twitter.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @NotEmpty(message = "The username is required.")
    private String username;

    @NotEmpty(message = "The password is required.")
    private String password;
}
