package org.example.twitter.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterRequest {
    @NotEmpty(message = "The username is required.")
    @Size(min = 2, max = 100, message = "The length of username must be between 2 and 100 characters.")
    private String username;

    @NotEmpty(message = "The password is required.")
    @Size(min = 8, message = "The length of password must be at least 8 characters.")
    private String password;

    @NotEmpty(message = "The email address is required.")
    @Email(message = "The email address is invalid.", flags = { Pattern.Flag.CASE_INSENSITIVE })
    private String email;
}
