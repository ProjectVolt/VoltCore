package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateContestDto;
import io.github.capure.voltcore.dto.GetContestDto;
import io.github.capure.voltcore.dto.GetContestPasswordDto;
import io.github.capure.voltcore.dto.PutContestDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.ContestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contest")
public class ContestController {
    @Autowired
    private ContestService contestService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public GetContestDto create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateContestDto data) {
        return contestService.create(user, data);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public GetContestDto edit(@AuthenticationPrincipal User user, @Valid @Min(1) @PathVariable Long id, @Valid @RequestBody PutContestDto data) throws InvalidIdException {
        return contestService.edit(user, id, data);
    }

    @GetMapping("/")
    public List<GetContestDto> getAll(@RequestParam(required = false) Boolean visible, @Valid @RequestParam @NotEmpty String search, @RequestParam @Min(0) Integer page, @RequestParam @Min(1) @Max(50) Integer pageSize) {
        return contestService.getAll(visible, search, page, pageSize);
    }

    @GetMapping("/{id}")
    public GetContestDto get(@AuthenticationPrincipal User user, @Valid @Min(1) @PathVariable Long id) throws InvalidIdException {
        return contestService.get(user, id, null);
    }

    @PostMapping("/{id}")
    public GetContestDto getWithPassword(@AuthenticationPrincipal User user, @Valid @Min(1) @PathVariable Long id, @Valid @RequestBody GetContestPasswordDto data) throws InvalidIdException {
        return contestService.get(user, id, data.getPassword());
    }
}
