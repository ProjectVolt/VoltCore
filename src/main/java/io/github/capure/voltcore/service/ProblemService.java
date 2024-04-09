package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.CreateProblemDto;
import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.dto.PutProblemDto;
import io.github.capure.voltcore.dto.admin.AdminGetProblemDto;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.dto.admin.PutTestCaseDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.InvalidIdRuntimeException;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.Tag;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.repository.ProblemRepository;
import io.github.capure.voltcore.util.Base64Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
            return problemRepository.findAllByNameLikeIgnoreCase(search, PageRequest.of(page, pageSize)).parallelStream()
                    .map(GetProblemDto::new)
                    .map(GetProblemDto::decode).toList();
        } else {
            return problemRepository.findAllByVisibleAndNameLikeIgnoreCase(visible, search, PageRequest.of(page, pageSize)).parallelStream()
                    .map(GetProblemDto::new)
                    .map(GetProblemDto::decode).toList();
        }
    }

    @PostAuthorize("returnObject.isVisible() || hasRole('ADMIN')")
    public GetProblemDto get(Long id) throws InvalidIdException {
        GetProblemDto problem = new GetProblemDto(problemRepository.findById(id).orElseThrow(InvalidIdException::new));
        return problem.decode();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional("transactionManager")
    public AdminGetProblemDto adminGet(Long id) throws InvalidIdException {
        AdminGetProblemDto problem = new AdminGetProblemDto(problemRepository.findById(id).orElseThrow(InvalidIdException::new));
        return problem.decode();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional("transactionManager")
    public AdminGetProblemDto create(CreateProblemDto data, User user) {
        Problem problem = new Problem();

        log.info("Adding new problem - name: {} addedBy: {} - {}", data.getName(), user.getId(), user.getUsername());

        problem.setVisible(data.isVisible());
        problem.setName(data.getName());
        problem.setDescription(Base64Helper.toBase64(data.getDescription()));
        problem.setLanguages(String.join(";", data.getLanguages()));
        problem.setTemplate(Base64Helper.toBase64(data.getTemplate()));
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
        saved.setTotalScore(createdTestCases.stream().map(TestCase::getMaxScore).reduce(0, Integer::sum));

        log.info("Saving the final problem data");
        saved = problemRepository.save(saved);
        log.info("Successfully created the problem");
        return new AdminGetProblemDto(saved).decode();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class})
    public AdminGetProblemDto edit(Long id, PutProblemDto data) throws InvalidIdException {
        Problem problem = problemRepository.findById(id).orElseThrow(InvalidIdException::new);

        log.info("Editing problem {} - {}", problem.getId(), problem.getName());

        if (data.getVisible() != null) problem.setVisible(data.getVisible());
        if (data.getName() != null) problem.setName(data.getName());
        if (data.getDescription() != null) problem.setDescription(Base64Helper.toBase64(data.getDescription()));
        if (data.getLanguages() != null) problem.setLanguages(String.join(";", data.getLanguages()));
        if (data.getTemplate() != null) problem.setTemplate(Base64Helper.toBase64(data.getTemplate()));
        if (data.getTimeLimit() != null) problem.setTimeLimit(data.getTimeLimit());
        if (data.getMemoryLimit() != null) problem.setMemoryLimit(data.getMemoryLimit());
        if (data.getDifficulty() != null) problem.setDifficulty(data.getDifficulty());
        if (data.getAuthor() != null) problem.setAuthor(data.getAuthor());
        if (data.getTransparentTestCases() != null) problem.setTransparentTestCases(data.getTransparentTestCases());

        if (data.getTags() != null) {
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
        }

        if (data.getTestCases() == null) {
            log.info("Saving problem");
            Problem saved = problemRepository.save(problem);
            log.info("Successfully saved the edited problem");
            return new AdminGetProblemDto(saved).decode();
        }

        log.info("Temporarily deleting all test cases from problem");
        Set<TestCase> oldTestCases = problem.getTestCases();
        problem.setTestCases(new HashSet<>());

        log.info("Saving problem");
        Problem saved = problemRepository.save(problem);
        log.info("Successfully saved the initial data");

        log.info("Deleting all completely removed test cases form db");
        List<Long> newIds = data.getTestCases().stream().map(PutTestCaseDto::getId).filter(Objects::nonNull).toList();
        List<TestCase> toDelete = oldTestCases.stream().filter(t -> !newIds.contains(t.getId())).toList();
        for (TestCase t : toDelete) {
            testCaseService.delete(t);
        }

        log.info("Editing all edited test cases");
        Set<TestCase> toAdd = new HashSet<>();
        List<PutTestCaseDto> toEdit = data.getTestCases().stream().filter(t -> Objects.nonNull(t.getId())).toList();
        for (PutTestCaseDto testCaseDto : toEdit) {
            Optional<TestCase> old = oldTestCases.stream().filter(t -> Objects.equals(t.getId(), testCaseDto.getId())).findFirst();
            if (old.isEmpty()) {
                log.info("Invalid test case id");
                throw new InvalidIdException("Invalid test case id");
            }
            toAdd.add(testCaseService.edit(testCaseDto, problem));
        }

        log.info("Adding new test cases");
        toAdd.addAll(data.getTestCases().stream().filter(t -> t.getId() == null).map(t -> testCaseService.create(new CreateTestCaseDto(t.getName(), t.getInput(), t.getOutput(), t.getMaxScore()), saved)).toList());

        log.info("Saving problem with new test cases");
        saved.setTestCases(toAdd);
        Problem result = problemRepository.save(saved);
        log.info("Successfully saved the edited problem");

        return new AdminGetProblemDto(result).decode();
    }
}
