package org.example.twitter.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewPostDto {
    @NotEmpty(message = "The username is required.")
    private String userId;

    @NotEmpty(message = "The content is required.")
    @Size(min = 2, max = 255, message = "The length of content must be between 2 and 255 characters.")
    private String content;
}
