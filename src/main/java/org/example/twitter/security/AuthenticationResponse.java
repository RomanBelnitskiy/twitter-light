package org.example.twitter.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String userId;
    private String token;
    private String status;
    private String error;
}
