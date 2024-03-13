package io.github.capure.voltcore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginDto {
    @NotNull @Size(min=3, max=50)
    private String username;
    @NotNull @Size(min=8, max=250)
    private String password;
}
