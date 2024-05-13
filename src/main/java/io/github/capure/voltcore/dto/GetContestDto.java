package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.model.Contest;
import io.github.capure.voltcore.model.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetContestDto {
    private Long id;
    private String name;
    private String description;
    private Long startTime;
    private Long endTime;
    private Boolean visible;
    private Long addedBy;
    private List<GetProblemDto> problems;

    public GetContestDto(Contest contest, Boolean unlocked) {
        this.setId(contest.getId());
        this.setName(contest.getName());
        this.setDescription(contest.getDescription());
        this.setStartTime(contest.getStartTime().getEpochSecond());
        this.setEndTime(contest.getEndTime().getEpochSecond());
        this.setVisible(contest.getVisible());
        this.setAddedBy(contest.getAddedBy().getId());

        if (unlocked) {
            Set<Problem> problems = contest.getProblems();
            this.setProblems(problems == null ? List.of() : problems.stream().map(GetProblemDto::new).toList());
        } else {
            this.setProblems(null);
        }
    }
}
