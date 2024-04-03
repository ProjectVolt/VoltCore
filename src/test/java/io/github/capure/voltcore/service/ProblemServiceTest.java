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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProblemService.class, TagService.class, TestCaseService.class, ProblemRepository.class})
@EnableMethodSecurity
public class ProblemServiceTest {
    @Autowired
    private ProblemService problemService;

    @MockBean
    private TagService tagService;

    @MockBean
    private TestCaseService testCaseService;

    @MockBean
    private ProblemRepository problemRepository;

    private User getUser() {
        return new User(1L,
                "admin",
                "password1",
                "tester@example.com",
                true,
                "ROLE_ADMIN",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0,
                Set.of());
    }

    private CreateProblemDto getData() {
        return new CreateProblemDto(false,
                "test problem",
                "this is a test",
                List.of("python"),
                null,
                1000,
                1000,
                "easy",
                List.of(),
                null,
                List.of(new CreateTestCaseDto("test", "in", "out", 10)),
                0);
    }

    @Test
    @WithMockUser
    public void getAllShouldThrowForNormalUserIfVisibleIsFalseOrNull() {
        assertThrows(AccessDeniedException.class, () -> problemService.getAll(false, "search", 0, 10));
        assertThrows(AccessDeniedException.class, () -> problemService.getAll(null, "search", 0, 10));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldWorkIfVisibleIsNull() {
        Problem problem = new Problem();
        problem.setId(7L);
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of());
        problem.setTags(Set.of());
        when(problemRepository.findAllByNameLikeIgnoreCase(any(), any())).thenReturn(List.of(problem));

        List<GetProblemDto> result = assertDoesNotThrow(() -> problemService.getAll(null, "test1*", 0, 10));

        assertEquals(7L, result.getFirst().getId());
    }

    @Test
    @WithMockUser()
    public void getAllShouldWorkIfVisibleIsSet() {
        Problem problem = new Problem();
        problem.setId(7L);
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of());
        problem.setTags(Set.of());
        problem.setVisible(true);
        when(problemRepository.findAllByVisibleAndNameLikeIgnoreCase(eq(true), any(), any())).thenReturn(List.of(problem));

        List<GetProblemDto> result = assertDoesNotThrow(() -> problemService.getAll(true, "test1*", 0, 10));

        assertEquals(7L, result.getFirst().getId());
    }

    @Test
    @WithMockUser
    public void getShouldWorkForVisibleAndNormalUser() {
        Problem problem = new Problem();
        problem.setId(5L);
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of());
        problem.setTags(Set.of());
        problem.setVisible(true);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        GetProblemDto problemDto = assertDoesNotThrow(() -> problemService.get(5L));

        assertEquals(5L, problemDto.getId());
    }

    @Test
    @WithMockUser
    public void getShouldThrowForInvisibleAndNormalUser() {
        Problem problem = new Problem();
        problem.setId(5L);
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of());
        problem.setTags(Set.of());
        problem.setVisible(false);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        assertThrows(AccessDeniedException.class, () -> problemService.get(5L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getShouldWorkForInvisibleAndAdmin() {
        Problem problem = new Problem();
        problem.setId(5L);
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of());
        problem.setTags(Set.of());
        problem.setVisible(false);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        GetProblemDto problemDto = assertDoesNotThrow(() -> problemService.get(5L));

        assertEquals(5L, problemDto.getId());
    }

    @Test
    @WithMockUser
    public void getShouldThrowForInvalidId() {
        when(problemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> problemService.get(5L));
    }

    @Test
    @WithMockUser
    public void createShouldThrowForNormalUser() {
        assertThrows(AccessDeniedException.class, () -> problemService.create(null, null));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldWorkForMinimalValidData() {
        CreateProblemDto data = getData();
        AtomicInteger createCalled = new AtomicInteger();
        AtomicInteger savedCalled = new AtomicInteger();
        AtomicReference<Problem> saved = new AtomicReference<>();
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            p.setId(1L);
            savedCalled.getAndIncrement();
            saved.set(p);
            return p;
        });
        when(testCaseService.create(any(), any())).thenAnswer(a -> {
            createCalled.getAndIncrement();
            CreateTestCaseDto testCaseData = a.getArgument(0);
            Problem p = a.getArgument(1);
            return new TestCase(1L, p, testCaseData.getName(), testCaseData.getInput(), testCaseData.getOutput(), testCaseData.getMaxScore());
        });

        GetProblemDto result = assertDoesNotThrow(() -> problemService.create(data, getUser()));

        assertEquals(2, savedCalled.get(), "save should be called before and after adding test cases");
        assertEquals(1, createCalled.get(), "test case should be created");
        assertEquals(data.getName(), result.getName(), "result should match the initial data");
        assertEquals(saved.get().getId(), result.getId(), "result should have the same id as saved");
        assertEquals(data.isVisible(), saved.get().isVisible());
        assertEquals(data.getName(), saved.get().getName());
        assertEquals(data.getDescription(), saved.get().getDescription());
        assertEquals(String.join(";", data.getLanguages()), saved.get().getLanguages());
        assertEquals(data.getTemplate(), saved.get().getTemplate());
        assertEquals(getUser().getUsername(), saved.get().getAddedBy().getUsername());
        assertEquals(data.getTimeLimit(), saved.get().getTimeLimit());
        assertEquals(data.getMemoryLimit(), saved.get().getMemoryLimit());
        assertEquals(data.getDifficulty(), saved.get().getDifficulty());
        assertEquals(data.getAuthor(), saved.get().getAuthor());
        assertEquals(data.getTransparentTestCases(), saved.get().getTransparentTestCases());
        assertEquals(data.getTestCases().stream().map(CreateTestCaseDto::getMaxScore).reduce(0, Integer::sum), saved.get().getTotalScore());
        assertEquals(0, saved.get().getSubmissionCount());
        assertEquals(0, saved.get().getAcceptedSubmissions());
        assertEquals(0, saved.get().getPartiallyAccepted());
        assertEquals(0, saved.get().getWrongSubmissions());
        assertEquals(0, saved.get().getRuntimeErrors());
        assertEquals(0, saved.get().getCompileErrors());
        assertEquals(data.getTestCases().getFirst().getName(), saved.get().getTestCases().stream().findFirst().get().getName());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldAddTagsForValidData() throws InvalidIdException {
        CreateProblemDto data = getData();
        data.setTags(List.of(1L, 2L));
        AtomicReference<Problem> saved = new AtomicReference<>();
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            p.setId(1L);
            saved.set(p);
            return p;
        });
        when(tagService.getById(any())).thenAnswer(a -> new Tag(a.getArgument(0), "tag", Set.of()));
        when(testCaseService.create(any(), any())).thenAnswer(a -> {
            CreateTestCaseDto testCaseData = a.getArgument(0);
            Problem p = a.getArgument(1);
            return new TestCase(1L, p, testCaseData.getName(), testCaseData.getInput(), testCaseData.getOutput(), testCaseData.getMaxScore());
        });

        assertDoesNotThrow(() -> problemService.create(data, getUser()));

        assertEquals(2, saved.get().getTags().size());
        assertEquals(1L, saved.get().getTags().stream().toList().getFirst().getId());
        assertEquals(2L, saved.get().getTags().stream().toList().getLast().getId());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldThrowForInvalidTagId() throws InvalidIdException {
        CreateProblemDto data = getData();
        data.setTags(List.of(1L, 2L));
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(tagService.getById(any())).thenThrow(InvalidIdException.class);
        when(testCaseService.create(any(), any())).thenAnswer(a -> {
            CreateTestCaseDto testCaseData = a.getArgument(0);
            Problem p = a.getArgument(1);
            return new TestCase(1L, p, testCaseData.getName(), testCaseData.getInput(), testCaseData.getOutput(), testCaseData.getMaxScore());
        });

        assertThrows(InvalidIdRuntimeException.class, () -> problemService.create(data, getUser()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldThrowForFailedTestCaseCreation() {
        CreateProblemDto data = getData();
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(testCaseService.create(any(), any())).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> problemService.create(data, getUser()));
    }
}
