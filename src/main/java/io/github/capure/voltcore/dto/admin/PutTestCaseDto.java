package io.github.capure.voltcore.dto.admin;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PutTestCaseDto extends CreateTestCaseDto {
    @Min(1)
    private Long id;
}
