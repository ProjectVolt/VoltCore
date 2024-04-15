package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.model.TestResult;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetTestResultDto {
    private Long id;
    private String output;
    private Integer cpuTime;
    private Integer realTime;
    private Integer memory;
    private Integer signal;
    private Integer exitCode;
    private String error;
    private String result;
    private Integer score;

    public GetTestResultDto(TestResult data, Boolean transparentTestCase) {
        id = data.getId();
        output = transparentTestCase ? data.getOutput() : null;
        cpuTime = data.getCpuTime();
        realTime = data.getRealTime();
        memory = data.getMemory();
        signal = data.getSignal();
        exitCode = data.getExitCode();
        error = data.getError();
        result = data.getResult();
        score = data.getScore();
    }
}
