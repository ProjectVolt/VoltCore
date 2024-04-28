package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateSubmissionDto;
import io.github.capure.voltcore.dto.GetSubmissionDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.ProblemNotVisibleException;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.SubmissionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/submission")
@Slf4j
public class SubmissionController {
    @Autowired
    private SubmissionService submissionService;

    @GetMapping("/{id}")
    public GetSubmissionDto get(@Valid @PathVariable @Min(1) Long id, @AuthenticationPrincipal User user) throws InvalidIdException {
        return submissionService.get(id, user);
    }

    @GetMapping("/problem/{problemId}")
    public List<GetSubmissionDto> getByUserAndProblemId(@Valid @PathVariable @Min(1) Long problemId, @AuthenticationPrincipal User user, @Valid @RequestParam @Min(1) Integer limit) throws InvalidIdException {
        return submissionService.getByUserAndProblemId(user, problemId, limit);
    }

    @PostMapping("/")
    public GetSubmissionDto create(HttpServletResponse response, @AuthenticationPrincipal User user, @Valid @RequestBody CreateSubmissionDto data) throws InvalidIdException, ProblemNotVisibleException, IOException {
        try {
            response.setStatus(201);
            return submissionService.create(data, user);
        } catch (RuntimeException ex) {
            log.error("Create failed", ex);
            response.sendError(500, "Server error");
        }
        return null;
    }
}
