package io.github.capure.voltcore.dto.admin;

import io.github.capure.voltcore.validator.ValidRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPutUserDto {
    @Size(min=8, max=250)
    private String password;
    private Boolean enabled;
    @ValidRole
    private String role;
    @Size(max=4096)
    private String avatar;
    @Size(max=50)
    private String github;
    @Size(max=100)
    private String school;
}
