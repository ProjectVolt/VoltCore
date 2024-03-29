package io.github.capure.voltcore.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTestCaseDto {
    @NotNull
    @Size(min = 2, max = 50)
    private String name;
    @NotNull
    @Size(max = 10000)
    private String input;
    @NotNull
    @Size(max = 10000)
    private String output;
    @NotNull
    @Min(1)
    @Max(10)
    private int maxScore;
}
