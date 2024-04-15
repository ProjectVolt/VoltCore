package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateSubmissionDto;
import io.github.capure.voltcore.dto.GetSubmissionDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.ProblemNotVisibleException;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.SubmissionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/submission")
@Slf4j
public class SubmissionController {
    @Autowired
    private SubmissionService submissionService;

    @GetMapping("/{id}")
    public GetSubmissionDto get(@Valid @PathVariable Long id, @AuthenticationPrincipal User user) throws InvalidIdException {
        return submissionService.get(id, user);
    }

    @PostMapping("/")
    public GetSubmissionDto create(HttpServletResponse response, @AuthenticationPrincipal User user, @Valid @RequestBody CreateSubmissionDto data) throws InvalidIdException, ProblemNotVisibleException, IOException {
        try {
            return submissionService.create(data, user);
        } catch (RuntimeException ex) {
            log.error("Create failed", ex);
            response.sendError(500, "Server error");
        }
        return null;
    }
}
