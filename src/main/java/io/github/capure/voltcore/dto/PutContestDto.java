package io.github.capure.voltcore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutContestDto {
    private Boolean visible;
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$")
    private String name;
    @Size(min = 2, max = 10000)
    private String description;
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$")
    private String password;
    @Min(1)
    private Long startTime;
    @Min(1)
    private Long endTime;
}
