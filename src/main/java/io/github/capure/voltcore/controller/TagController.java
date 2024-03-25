package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.CreateTagDto;
import io.github.capure.voltcore.dto.GetTagDto;
import io.github.capure.voltcore.exception.FailedCreateException;
import io.github.capure.voltcore.service.TagService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping("/")
    public List<GetTagDto> getAll(HttpServletResponse response, @RequestParam @NotEmpty String search, @RequestParam @Min(0) Integer page, @RequestParam @Min(1) @Max(50) Integer pageSize) {
        response.setStatus(200);
        return tagService.getAll(search, page, pageSize);
    }

    @PostMapping("/")
    @ResponseBody
    public GetTagDto create(HttpServletResponse response, @Valid @RequestBody CreateTagDto tagData) throws IOException {
        try {
            return tagService.create(tagData.getName());
        } catch (FailedCreateException e) {
            response.sendError(500, "Unexpected state");
            return null;
        }
    }
}
