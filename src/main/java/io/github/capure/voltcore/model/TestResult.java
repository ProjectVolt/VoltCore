package io.github.capure.voltcore.model;

import io.github.capure.schema.AvroJudgerResult;
import io.github.capure.schema.AvroTestCaseResult;
import io.github.capure.voltcore.dto.SubmissionStatus;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.InvalidIdRuntimeException;
import io.github.capure.voltcore.util.Base64Helper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class TestResult {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    @NotNull
    private String testCaseName;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;
    @NotNull
    private Integer cpuTime;
    @NotNull
    private Integer realTime;
    @NotNull
    private Integer memory;
    @NotNull
    private Integer signal;
    @NotNull
    private Integer exitCode;
    @NotNull
    private String error;
    @NotNull
    private String result;
    @NotNull
    private Integer score;
    @NotNull
    private Integer maxScore;

    public TestResult(AvroTestCaseResult data, Submission submission) {
        this.submission = submission;

        log.info("Attempting to match the test result with the test case {}", data.getTestCaseId());
        TestCase testCase = submission.getProblem().getTestCases().stream()
                .filter(t -> t.getId().equals(data.getTestCaseId()))
                .findAny().orElseThrow(InvalidIdRuntimeException::new);
        log.info("Matched successfully");
        testCaseName = testCase.getName();
        input = Base64Helper.fromBase64(testCase.getInput());
        maxScore = testCase.getMaxScore();

        output = data.getOutput().toString();
        AvroJudgerResult judgerResult = data.getJudgerResult();
        cpuTime = judgerResult.getCpuTime();
        realTime = judgerResult.getRealTime();
        memory = judgerResult.getMemory();
        signal = judgerResult.getSignal();
        exitCode = judgerResult.getExitCode();
        error = data.getErrorMessage().toString();
        result = SubmissionStatus.fromAvro(judgerResult.getResult());
        score = data.getScore();
    }
}
