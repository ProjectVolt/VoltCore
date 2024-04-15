package io.github.capure.voltcore.model;

import io.github.capure.schema.AvroJudgerResult;
import io.github.capure.schema.AvroTestCaseResult;
import io.github.capure.voltcore.dto.SubmissionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestResult {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
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

    public TestResult(AvroTestCaseResult data, Submission submission) {
        this.submission = submission;
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
