package io.github.capure.voltcore.dto.admin;

import io.github.capure.voltcore.model.TestCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTestCaseDto {
    private Long id;
    private String name;
    private String input;
    private String output;
    private int maxScore;

    public GetTestCaseDto(TestCase testCase) {
        id = testCase.getId();
        name = testCase.getName();
        input = testCase.getInput();
        output = testCase.getOutput();
        maxScore = testCase.getMaxScore();
    }
}
