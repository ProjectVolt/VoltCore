package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateProblemDto;
import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.dto.PutProblemDto;
import io.github.capure.voltcore.dto.admin.AdminGetProblemDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.ProblemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/problem")
public class ProblemController {
    @Autowired
    private ProblemService problemService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminGetProblemDto create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateProblemDto data) throws IOException {
        return problemService.create(data, user);
    }

    @PutMapping("/{id}")
    public AdminGetProblemDto edit(@Valid @PathVariable Long id, @AuthenticationPrincipal User user, @Valid @RequestBody PutProblemDto data) throws IOException, InvalidIdException {
        return problemService.edit(id, data);
    }

    @GetMapping("/{id}")
    public GetProblemDto getById(@Valid @PathVariable Long id) throws InvalidIdException {
        return problemService.get(id);
    }

    @GetMapping("/admin/{id}")
    public AdminGetProblemDto adminGetById(@Valid @PathVariable Long id) throws InvalidIdException {
        return problemService.adminGet(id);
    }

    @GetMapping("/")
    public List<GetProblemDto> getAll(@RequestParam(required = false) Boolean visible, @RequestParam @NotEmpty String search, @RequestParam @Min(0) Integer page, @RequestParam @Min(1) @Max(50) Integer pageSize) {
        return problemService.getAll(visible, search, page, pageSize);
    }
}
