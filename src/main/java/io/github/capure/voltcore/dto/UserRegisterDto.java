package io.github.capure.voltcore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisterDto {
    @NotNull
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String username;
    @NotNull
    @Size(min = 8, max = 250)
    private String password;
    @NotNull
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?``{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Please provide a valid email address")
    private String email;
    @Size(max = 50)
    private String github;
    @Size(max = 100)
    private String school;
}
