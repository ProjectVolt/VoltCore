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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.github.capure.voltcore.util.Base64Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
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
        problem.setDescription(Base64.getEncoder().encodeToString("test".getBytes()));
        problem.setTemplate(Base64.getEncoder().encodeToString("test".getBytes()));
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of());
        problem.setTags(Set.of());
        problem.setVisible(true);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        GetProblemDto problemDto = assertDoesNotThrow(() -> problemService.get(5L));

        assertEquals(5L, problemDto.getId());
        assertEquals("test", problemDto.getDescription());
        assertEquals("test", problemDto.getTemplate());
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
    public void adminGetShouldThrowForNormalUser() {
        assertThrows(AccessDeniedException.class, () -> problemService.adminGet(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetShouldWorkForAdminUser() {
        Problem problem = new Problem();
        problem.setId(5L);
        problem.setDescription(Base64.getEncoder().encodeToString("test".getBytes()));
        problem.setTemplate(Base64.getEncoder().encodeToString("test".getBytes()));
        problem.setLanguages("python");
        problem.setAddedBy(getUser());
        problem.setTestCases(Set.of(new TestCase(1L, null, "test case 1",
                Base64.getEncoder().encodeToString("in".getBytes()),
                Base64.getEncoder().encodeToString("out".getBytes()),
                10)));
        problem.setTags(Set.of());
        problem.setVisible(true);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        AdminGetProblemDto problemDto = assertDoesNotThrow(() -> problemService.adminGet(5L));

        assertEquals(5L, problemDto.getId());
        assertEquals("test", problemDto.getDescription());
        assertEquals("test", problemDto.getTemplate());
        assertEquals("in", problemDto.getTestCases().getFirst().getInput());
        assertEquals("out", problemDto.getTestCases().getFirst().getOutput());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetShouldThrowForInvalidId() {
        when(problemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> problemService.adminGet(5L));
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
        assertEquals(Base64.getEncoder().encodeToString(data.getDescription().getBytes()), saved.get().getDescription());
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

    @Test
    @WithMockUser
    public void editShouldThrowForNormalUser() {
        assertThrows(AccessDeniedException.class, () -> problemService.edit(null, null));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldThrowForInvalidId() {
        when(problemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> problemService.edit(1L, null));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldSetDataIfPresent() {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setAddedBy(getUser());
        problem.setTags(new HashSet<>());
        problem.setTestCases(new HashSet<>());
        PutProblemDto data = new PutProblemDto();
        data.setVisible(true);
        data.setName("test");
        data.setDescription("testtest");
        data.setLanguages(List.of("python", "c"));
        data.setTemplate("#template");
        data.setTimeLimit(3000);
        data.setMemoryLimit(256);
        data.setDifficulty("easy");
        data.setAuthor("tester");
        data.setTransparentTestCases(1);
        AtomicReference<Problem> saved = new AtomicReference<>(null);
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });

        AdminGetProblemDto result = assertDoesNotThrow(() -> problemService.edit(1L, data));

        assertNotNull(saved.get());
        assertEquals(Base64Helper.toBase64(data.getDescription()), saved.get().getDescription());
        assertEquals(data.getVisible(), result.isVisible());
        assertEquals(data.getName(), result.getName());
        assertEquals(data.getDescription(), result.getDescription());
        assertArrayEquals(data.getLanguages().toArray(new String[0]), result.getLanguages().toArray(new String[0]));
        assertEquals(data.getTemplate(), result.getTemplate());
        assertEquals(data.getTimeLimit(), result.getTimeLimit());
        assertEquals(data.getMemoryLimit(), result.getMemoryLimit());
        assertEquals(data.getDifficulty(), result.getDifficulty());
        assertEquals(data.getAuthor(), result.getAuthor());
        assertEquals(data.getTransparentTestCases(), result.getTransparentTestCases());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldAddTagsForValidData() throws InvalidIdException {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setAddedBy(getUser());
        problem.setTags(new HashSet<>());
        problem.setTestCases(new HashSet<>());
        problem.setLanguages("python");
        PutProblemDto data = new PutProblemDto();
        data.setTags(List.of(1L, 2L));
        AtomicReference<Problem> saved = new AtomicReference<>();
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            saved.set(p);
            return p;
        });
        when(tagService.getById(any())).thenAnswer(a -> new Tag(a.getArgument(0), "tag", Set.of()));

        assertDoesNotThrow(() -> problemService.edit(1L, data));

        assertEquals(2, saved.get().getTags().size());
        assertEquals(1L, saved.get().getTags().stream().toList().getFirst().getId());
        assertEquals(2L, saved.get().getTags().stream().toList().getLast().getId());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldThrowForInvalidTagId() throws InvalidIdException {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setAddedBy(getUser());
        problem.setTags(new HashSet<>());
        problem.setTestCases(new HashSet<>());
        problem.setLanguages("python");
        PutProblemDto data = new PutProblemDto();
        data.setTags(List.of(1L, 2L));
        AtomicReference<Problem> saved = new AtomicReference<>();
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            saved.set(p);
            return p;
        });
        when(tagService.getById(any())).thenThrow(InvalidIdException.class);

        assertThrows(InvalidIdRuntimeException.class, () -> problemService.edit(1L, data));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldUpdateTestCasesForValidData() {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setAddedBy(getUser());
        problem.setTags(new HashSet<>());
        problem.setLanguages("python");
        TestCase testTestCase1 = new TestCase(1L, problem, "Test", Base64Helper.toBase64("in"), Base64Helper.toBase64("out"), 7);
        TestCase testTestCase2 = new TestCase(2L, problem, "Another test", Base64Helper.toBase64("in"), Base64Helper.toBase64("out"), 3);
        problem.setTestCases(new HashSet<>(Set.of(testTestCase1, testTestCase2)));
        PutProblemDto data = new PutProblemDto();
        PutTestCaseDto putTestCase2 = new PutTestCaseDto();
        putTestCase2.setId(testTestCase2.getId());
        putTestCase2.setName("New name");
        putTestCase2.setInput(Base64Helper.fromBase64(testTestCase2.getInput()));
        putTestCase2.setOutput(Base64Helper.fromBase64(testTestCase2.getOutput()));
        putTestCase2.setMaxScore(testTestCase2.getMaxScore());
        PutTestCaseDto putTestCase3 = new PutTestCaseDto();
        putTestCase3.setName("New test case");
        putTestCase3.setInput("in");
        putTestCase3.setOutput("out");
        putTestCase3.setMaxScore(7);
        data.setTestCases(List.of(putTestCase2, putTestCase3));
        AtomicReference<Problem> saved = new AtomicReference<>(null);
        AtomicReference<Boolean> deleteCalled = new AtomicReference<>(false);
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem p = a.getArgument(0);
            if (saved.get() == null) {
                assertTrue(p.getTestCases().isEmpty(), "Initial save should remove all test cases");
            }
            saved.set(p);
            return p;
        });
        Mockito.doAnswer(a -> {
            TestCase arg = a.getArgument(0);
            assertEquals(testTestCase1.getId(), arg.getId());
            deleteCalled.set(true);
            return null;
        }).when(testCaseService).delete(any());
        when(testCaseService.edit(any(), any())).thenAnswer(a -> {
            PutTestCaseDto arg = a.getArgument(0);
            assertEquals(testTestCase2.getId(), arg.getId());
            return new TestCase(arg.getId(), problem, arg.getName(), Base64Helper.toBase64(arg.getInput()), Base64Helper.toBase64(arg.getOutput()), arg.getMaxScore());
        });
        when(testCaseService.create(any(), any())).thenAnswer(a -> {
            CreateTestCaseDto arg = a.getArgument(0);
            assertEquals(putTestCase3.getName(), arg.getName());
            return new TestCase(3L, problem, arg.getName(), Base64Helper.toBase64(arg.getInput()), Base64Helper.toBase64(arg.getOutput()), arg.getMaxScore());
        });

        AdminGetProblemDto result = assertDoesNotThrow(() -> problemService.edit(1L, data));

        assertTrue(deleteCalled.get(), "Delete should be called for the first test case");
        assertEquals(2, result.getTestCases().size());
        assertEquals(putTestCase2.getId(), result.getTestCases().getFirst().getId());
        assertEquals(putTestCase2.getName(), result.getTestCases().getFirst().getName());
        assertEquals(putTestCase2.getInput(), result.getTestCases().getFirst().getInput());
        assertEquals(putTestCase2.getOutput(), result.getTestCases().getFirst().getOutput());
        assertEquals(putTestCase2.getMaxScore(), result.getTestCases().getFirst().getMaxScore());
        assertEquals(3L, result.getTestCases().getLast().getId());
        assertEquals(putTestCase3.getName(), result.getTestCases().getLast().getName());
        assertEquals(putTestCase3.getInput(), result.getTestCases().getLast().getInput());
        assertEquals(putTestCase3.getOutput(), result.getTestCases().getLast().getOutput());
        assertEquals(putTestCase3.getMaxScore(), result.getTestCases().getLast().getMaxScore());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldThrowForInvalidTestCaseId() {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setAddedBy(getUser());
        problem.setTags(new HashSet<>());
        problem.setLanguages("python");
        TestCase testTestCase1 = new TestCase(1L, problem, "Test", Base64Helper.toBase64("in"), Base64Helper.toBase64("out"), 7);
        problem.setTestCases(new HashSet<>(Set.of(testTestCase1)));
        PutProblemDto data = new PutProblemDto();
        PutTestCaseDto putTestCase1 = new PutTestCaseDto();
        putTestCase1.setId(777L);
        putTestCase1.setName("New name");
        putTestCase1.setInput(Base64Helper.fromBase64(testTestCase1.getInput()));
        putTestCase1.setOutput(Base64Helper.fromBase64(testTestCase1.getOutput()));
        putTestCase1.setMaxScore(testTestCase1.getMaxScore());
        data.setTestCases(List.of(putTestCase1));
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any())).thenAnswer(a -> a.<Problem>getArgument(0));
        when(testCaseService.edit(any(), any())).thenAnswer(a -> {
            PutTestCaseDto arg = a.getArgument(0);
            fail("Edit shouldn't be called, the id is incorrect");
            return new TestCase(arg.getId(), problem, arg.getName(), Base64Helper.toBase64(arg.getInput()), Base64Helper.toBase64(arg.getOutput()), arg.getMaxScore());
        });

        assertThrows(InvalidIdException.class, () -> problemService.edit(1L, data));
    }
}
