package io.github.capure.voltcore.dto.admin;

import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.model.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminGetProblemDto extends GetProblemDto {
    private List<GetTestCaseDto> testCases;

    private String fromBase64(String in) {
        if (in == null) return null;
        return new String(Base64.getDecoder().decode(in));
    }

    public AdminGetProblemDto(Problem p) {
        super(p);
        testCases = p.getTestCases().stream().map(GetTestCaseDto::new).toList();
    }
}
