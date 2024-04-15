package io.github.capure.voltcore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSubmissionDto {
    @NotNull
    @Min(1)
    private Long problemId;
    @NotNull
    @Size(min = 1, max = 50000)
    private String sourceCode;
    @NotNull
    @NotEmpty
    @Pattern(regexp = "(python)|(cpp)|(c)")
    private String language;
}
