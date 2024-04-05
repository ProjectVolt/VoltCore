package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateProblemDto;
import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.dto.admin.AdminGetProblemDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.InvalidIdRuntimeException;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.ProblemService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/problem")
@Slf4j
public class ProblemController {
    @Autowired
    private ProblemService problemService;

    @PostMapping("/")
    public GetProblemDto create(HttpServletResponse response, @AuthenticationPrincipal User user, @Valid @RequestBody CreateProblemDto data) throws IOException {
        try {
            return problemService.create(data, user);
        } catch (InvalidIdRuntimeException ex) {
            response.sendError(400, "Invalid tag id encountered");
        } catch (AccessDeniedException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Create failed", ex);
            response.sendError(500, "Server error");
        }
        return null;
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
    public List<GetProblemDto> getAll(HttpServletResponse response, @RequestParam(required = false) Boolean visible, @RequestParam @NotEmpty String search, @RequestParam @Min(0) Integer page, @RequestParam @Min(1) @Max(50) Integer pageSize) {
        response.setStatus(200);
        return problemService.getAll(visible, search, page, pageSize);
    }
}
