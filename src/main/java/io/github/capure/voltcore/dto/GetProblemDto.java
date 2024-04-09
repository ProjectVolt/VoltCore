package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.util.Base64Helper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProblemDto {
    private Long id;
    private boolean visible;
    private String name;
    private String description;
    private List<String> languages;
    private String template;
    private Long addedBy;
    private int timeLimit;
    private int memoryLimit;
    private String difficulty;
    private List<GetTagDto> tags;
    private String author;
    private int transparentTestCases;
    private int totalScore;
    private int submissionCount;
    private int acceptedSubmissions;
    private int wrongSubmissions;
    private int partiallyAccepted;
    private int runtimeErrors;
    private int compileErrors;

    public GetProblemDto(Problem p) {
        setId(p.getId());
        setVisible(p.isVisible());
        setName(p.getName());
        setDescription(p.getDescription());
        setLanguages(Arrays.stream(p.getLanguages().split(";")).toList());
        setTemplate(p.getTemplate());
        setAddedBy(p.getAddedBy().getId());
        setTimeLimit(p.getTimeLimit());
        setMemoryLimit(p.getMemoryLimit());
        setDifficulty(p.getDifficulty());
        setTags(p.getTags().stream().map(GetTagDto::new).toList());
        setAuthor(p.getAuthor());
        setTransparentTestCases(p.getTransparentTestCases());
        setTotalScore(p.getTotalScore());
        setSubmissionCount(p.getSubmissionCount());
        setAcceptedSubmissions(p.getAcceptedSubmissions());
        setWrongSubmissions(p.getWrongSubmissions());
        setPartiallyAccepted(p.getPartiallyAccepted());
        setRuntimeErrors(p.getRuntimeErrors());
        setCompileErrors(p.getCompileErrors());
    }

    public GetProblemDto decode() {
        this.setDescription(Base64Helper.fromBase64(this.getDescription()));
        this.setTemplate(Base64Helper.fromBase64(this.getTemplate()));
        return this;
    }
}
