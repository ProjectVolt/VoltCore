package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.model.Submission;
import io.github.capure.voltcore.model.TestResult;
import io.github.capure.voltcore.util.Base64Helper;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetSubmissionDto {
    private Long id;
    private Long problemId;
    private Long createdOn;
    private Long addedBy;
    private String sourceCode;
    private String language;
    private String status;
    private Boolean compileSuccess;
    private Boolean runSuccess;
    private Boolean answerSuccess;
    private String compileErrorMessage;
    private Boolean compileErrorFatal;
    private List<GetTestResultDto> testResults;
    private Integer maxCpu;
    private Integer maxMemory;
    private Integer score;

    public GetSubmissionDto(Submission submission, Boolean showSourceCode) {
        id = submission.getId();
        problemId = submission.getProblem().getId();
        createdOn = submission.getCreatedOn() != null ? submission.getCreatedOn().getEpochSecond() : 0;
        addedBy = submission.getAddedBy().getId();
        sourceCode = showSourceCode ? Base64Helper.fromBase64(submission.getSourceCode()) : null;
        language = submission.getLanguage();
        status = submission.getStatus();
        compileSuccess = submission.getCompileSuccess();
        runSuccess = submission.getRunSuccess();
        answerSuccess = submission.getAnswerSuccess();
        compileErrorMessage = submission.getCompileErrorMessage();
        compileErrorFatal = submission.getCompileErrorFatal();
        maxCpu = submission.getMaxCpu();
        maxMemory = submission.getMaxMemory();
        score = submission.getScore();

        testResults = new ArrayList<>();
        int transparent = submission.getProblem().getTransparentTestCases();
        for (TestResult testResult : submission.getTestResults()) {
            testResults.add(new GetTestResultDto(testResult, transparent > 0));
            if (transparent != 0) transparent--;
        }
    }
}
