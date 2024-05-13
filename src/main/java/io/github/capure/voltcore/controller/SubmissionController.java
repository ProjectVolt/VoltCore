package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateSubmissionDto;
import io.github.capure.voltcore.dto.GetSubmissionDto;
import io.github.capure.voltcore.exception.ContestClosedException;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.ProblemNotVisibleException;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.SubmissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {
    @Autowired
    private SubmissionService submissionService;

    @GetMapping("/{id}")
    public GetSubmissionDto get(@Valid @PathVariable @Min(1) Long id, @AuthenticationPrincipal User user) throws InvalidIdException {
        return submissionService.get(id, user);
    }

    @GetMapping("/problem/{problemId}")
    public List<GetSubmissionDto> getByUserAndProblemId(@Valid @PathVariable @Min(1) Long problemId, @AuthenticationPrincipal User user, @Valid @RequestParam @Min(1) Integer limit) {
        return submissionService.getByUserAndProblemId(user, problemId, limit);
    }

    @GetMapping("/")
    public List<GetSubmissionDto> getAll(@Valid @RequestParam(required = false) @Min(1) Long contestId, @Valid @RequestParam @Min(0) Integer page, @Valid @RequestParam @Min(1) @Max(50) Integer pageSize) throws InvalidIdException {
        return submissionService.getAll(contestId, page, pageSize);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public GetSubmissionDto create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateSubmissionDto data) throws InvalidIdException, ProblemNotVisibleException, IOException, ContestClosedException {
        return submissionService.create(data, user);
    }

}
