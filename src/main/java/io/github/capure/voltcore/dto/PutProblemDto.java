package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.dto.admin.PutTestCaseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutProblemDto {
    private Boolean visible;
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$")
    private String name;
    @Size(min = 2, max = 10000)
    private String description;
    @Size(min = 1, max = 3)
    private List<@Pattern(regexp = "(python)|(cpp)|(c)") String> languages;
    @Size(min = 1, max = 10000)
    private String template;
    @Min(100)
    @Max(20000)
    private Integer timeLimit;
    @Min(100)
    @Max(2000)
    private Integer memoryLimit;
    @Pattern(regexp = "(easy)|(medium)|(hard)")
    private String difficulty;
    private List<@Min(1) Long> tags;
    @Size(min = 2, max = 50)
    private String author;
    @Size(max = 100)
    private List<@Valid PutTestCaseDto> testCases;
    @Min(0)
    @Max(100)
    private Integer transparentTestCases;
}
