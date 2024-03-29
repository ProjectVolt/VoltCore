package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.CreateProblemDto;
import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.InvalidIdRuntimeException;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.Tag;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.repository.ProblemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProblemService {
    @Autowired
    TagService tagService;

    @Autowired
    TestCaseService testCaseService;

    @Autowired
    ProblemRepository problemRepository;

    @PreAuthorize("(#visible != null && #visible) || hasRole('ADMIN')")
    @Transactional
    public List<GetProblemDto> getAll(Boolean visible, String search, int page, int pageSize) {
        if (visible == null) {
            return problemRepository.findAllByNameLikeIgnoreCase(search, PageRequest.of(page, pageSize)).parallelStream().map(GetProblemDto::new).toList();
        } else {
            return problemRepository.findAllByVisibleAndNameLikeIgnoreCase(visible, search, PageRequest.of(page, pageSize)).parallelStream().map(GetProblemDto::new).toList();
        }
    }

    @PostAuthorize("returnObject.isVisible() || hasRole('ADMIN')")
    public GetProblemDto get(Long id) throws InvalidIdException {
        return new GetProblemDto(problemRepository.findById(id).orElseThrow(InvalidIdException::new));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GetProblemDto create(CreateProblemDto data, User user) {
        Problem problem = new Problem();

        log.info("Adding new problem - name: {} addedBy: {} - {}", data.getName(), user.getId(), user.getUsername());

        problem.setVisible(data.isVisible());
        problem.setName(data.getName());
        problem.setDescription(data.getDescription());
        problem.setLanguages(String.join(";", data.getLanguages()));
        problem.setTemplate(data.getTemplate());
        problem.setAddedBy(user);
        problem.setTimeLimit(data.getTimeLimit());
        problem.setMemoryLimit(data.getMemoryLimit());
        problem.setDifficulty(data.getDifficulty());
        problem.setAuthor(data.getAuthor());
        problem.setTransparentTestCases(data.getTransparentTestCases());

        problem.setTotalScore(0);
        problem.setSubmissionCount(0);
        problem.setAcceptedSubmissions(0);
        problem.setWrongSubmissions(0);
        problem.setPartiallyAccepted(0);
        problem.setRuntimeErrors(0);
        problem.setCompileErrors(0);

        log.info("Getting tag data");
        Set<Tag> tags = data.getTags().stream().map(tId -> {
            try {
                return tagService.getById(tId);
            } catch (InvalidIdException e) {
                log.info("Encountered a tag with an invalid id");
                throw new InvalidIdRuntimeException();
            }
        }).collect(Collectors.toSet());
        problem.setTags(tags);
        log.info("Tags found successfully");


        log.info("Saving problem");
        Problem saved = problemRepository.save(problem);
        log.info("Successfully saved the initial data");

        log.info("Creating test cases");
        Set<TestCase> createdTestCases = new HashSet<>();
        for (CreateTestCaseDto testCase : data.getTestCases()) {
            createdTestCases.add(testCaseService.create(testCase, saved));
        }
        saved.setTestCases(createdTestCases);

        log.info("Saving the final problem data");
        saved = problemRepository.save(saved);
        log.info("Successfully created the problem");
        return new GetProblemDto(saved);
    }
}
