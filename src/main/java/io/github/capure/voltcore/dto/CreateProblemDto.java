package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProblemDto {
    @NotNull
    private boolean visible;
    @NotNull
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String name;
    @NotNull
    @Size(min = 2, max = 10000)
    private String description;
    @NotNull
    @Size(min = 1, max = 3)
    private List<@Pattern(regexp = "^(\bpython\b)|(\bcpp\b)|(\bc\b)+$") String> languages;
    @Size(min = 1, max = 10000)
    private String template;
    @NotNull
    @Min(100)
    @Max(20000)
    private int timeLimit;
    @NotNull
    @Min(100)
    @Max(2000)
    private int memoryLimit;
    @NotNull
    @Pattern(regexp = "^(\beasy\b)|(\bmedium\b)|(\bhard\b)+$")
    private String difficulty;
    @NotNull
    private List<@Min(1) Long> tags;
    @Size(min = 2, max = 50)
    private String author;
    @NotNull
    @Size(min = 1, max = 100)
    private List<@Valid CreateTestCaseDto> testCases;
    @NotNull
    @Min(0)
    @Max(100)
    private int transparentTestCases;
}
