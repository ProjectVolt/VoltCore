package io.github.capure.voltcore.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PutUserDto {
    @Size(max=4096)
    private String avatar;
    @Size(max=60)
    private String github;
    @Size(max=60)
    private String school;
}
