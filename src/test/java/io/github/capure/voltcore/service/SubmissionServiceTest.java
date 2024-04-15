package io.github.capure.voltcore.service;

import io.github.capure.schema.*;
import io.github.capure.voltcore.dto.CreateSubmissionDto;
import io.github.capure.voltcore.dto.GetSubmissionDto;
import io.github.capure.voltcore.dto.SubmissionStatus;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.ProblemNotVisibleException;
import io.github.capure.voltcore.model.*;
import io.github.capure.voltcore.repository.ProblemRepository;
import io.github.capure.voltcore.repository.SubmissionRepository;
import io.github.capure.voltcore.repository.TestResultRepository;
import io.github.capure.voltcore.util.Base64Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SubmissionService.class})
@EnableMethodSecurity
public class SubmissionServiceTest {
    @Autowired
    private SubmissionService submissionService;

    @MockBean
    private ProblemRepository problemRepository;

    @MockBean
    private SubmissionRepository submissionRepository;

    @MockBean
    private TestResultRepository testResultRepository;

    @MockBean
    private KafkaTemplate<String, AvroSubmission> kafkaTemplate;

    private User getUser(Boolean admin) {
        return new User(1L,
                admin ? "admin" : "tester",
                "password1",
                "tester@example.com",
                true,
                admin ? "ROLE_ADMIN" : "ROLE_USER",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0,
                Set.of(),
                Set.of());
    }

    private Problem getProblem() {
        Problem problem = new Problem(1L,
                true,
                "test problem",
                "description",
                "python;c;cpp",
                null,
                getUser(false),
                1000,
                1000 * 1024 * 1024,
                "easy",
                Set.of(),
                null,
                Set.of(),
                Set.of(),
                1,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        );
        TestCase testCase = new TestCase(1L, problem, "test case", Base64Helper.toBase64("in"), Base64Helper.toBase64("out"), 10);
        problem.setTestCases(Set.of(testCase));
        return problem;
    }

    private Submission getSubmission() {
        Problem problem = getProblem();
        Submission submission = new Submission(1L,
                problem,
                Instant.now(),
                getUser(false),
                Base64Helper.toBase64("source code"),
                "python",
                SubmissionStatus.Pending,
                false,
                false,
                false,
                null,
                false,
                List.of(),
                0,
                0,
                0);
        problem.setSubmissions(Set.of(submission));
        return submission;
    }

    private CreateSubmissionDto getCreateSubmissionDto() {
        Submission submission = getSubmission();
        return new CreateSubmissionDto(
                submission.getProblem().getId(),
                Base64Helper.fromBase64(submission.getSourceCode()),
                submission.getLanguage()
        );
    }

    private AvroSubmissionResult getSubmissionResult() {
        Submission submission = getSubmission();
        AvroTestCaseResult test1 = AvroTestCaseResult.newBuilder()
                .setTestCaseId(1L)
                .setJudgerResult(AvroJudgerResult.newBuilder()
                        .setResult(AvroJudgerResultCode.RESULT_SUCCESS)
                        .setCpuTime(300)
                        .setRealTime(120)
                        .setMemory(8000000)
                        .setSignal(0)
                        .setExitCode(0)
                        .setError(AvroJudgerResultError.ERROR_NONE)
                        .build())
                .setOutput("out")
                .setErrorMessage("")
                .setScore(7).build();
        AvroTestCaseResult test2 = AvroTestCaseResult.newBuilder()
                .setTestCaseId(2L)
                .setJudgerResult(AvroJudgerResult.newBuilder()
                        .setResult(AvroJudgerResultCode.RESULT_WRONG_ANSWER)
                        .setCpuTime(250)
                        .setRealTime(120)
                        .setMemory(5000000)
                        .setSignal(0)
                        .setExitCode(0)
                        .setError(AvroJudgerResultError.ERROR_NONE)
                        .build())
                .setOutput("out")
                .setErrorMessage("")
                .setScore(0).build();
        AvroSubmissionResult result = new AvroSubmissionResult();
        result.setSubmissionId(submission.getId());
        result.setProblemId(submission.getProblem().getId());
        result.setAnswerSuccess(false);
        result.setCompileSuccess(true);
        result.setRunSuccess(true);
        result.setCompileError(new AvroCompileError("", false));
        result.setTestResults(List.of(test1, test2));
        return result;
    }

    @Test
    public void getShouldThrowForInvalidId() {
        when(submissionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> submissionService.get(1L, getUser(false)));
    }

    @Test
    public void getShouldShowCodeForOwner() {
        when(submissionRepository.findById(any())).thenReturn(Optional.of(getSubmission()));

        GetSubmissionDto result = assertDoesNotThrow(() -> submissionService.get(1L, getUser(false)));

        assertNotNull(result.getSourceCode());
    }

    @Test
    public void getShouldShowCodeForAdmin() {
        when(submissionRepository.findById(any())).thenReturn(Optional.of(getSubmission()));
        User admin = getUser(true);
        admin.setId(2L);

        GetSubmissionDto result = assertDoesNotThrow(() -> submissionService.get(1L, admin));

        assertNotNull(result.getSourceCode());
    }

    @Test
    public void getShouldHideCodeForOtherUser() {
        when(submissionRepository.findById(any())).thenReturn(Optional.of(getSubmission()));
        User user = getUser(false);
        user.setId(2L);

        GetSubmissionDto result = assertDoesNotThrow(() -> submissionService.get(1L, user));

        assertNull(result.getSourceCode());
    }

    @Test
    public void createShouldThrowForInvalidProblemId() {
        when(problemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> submissionService.create(getCreateSubmissionDto(), getUser(false)));
    }

    @Test
    public void createShouldThrowForInvisibleProblem() {
        Problem problem = getProblem();
        problem.setVisible(false);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        assertThrows(ProblemNotVisibleException.class, () -> submissionService.create(getCreateSubmissionDto(), getUser(false)));
    }

    @Test
    public void createShouldWorkForValidData() {
        when(problemRepository.findById(any())).thenReturn(Optional.of(getProblem()));
        AtomicReference<Submission> saved = new AtomicReference<>(null);
        AtomicReference<AvroSubmission> sentToKafka = new AtomicReference<>(null);
        when(submissionRepository.save(any())).thenAnswer(a -> {
            Submission arg = a.getArgument(0);
            arg.setId(1L);
            saved.set(arg);
            return arg;
        });
        when(kafkaTemplate.send(any(), any(), any())).thenAnswer(a -> {
            assertEquals("submissions", a.getArgument(0));
            assertEquals(getProblem().getId().toString(), a.getArgument(1));
            AvroSubmission arg = a.getArgument(2);
            sentToKafka.set(arg);
            return CompletableFuture.completedFuture(null);
        });

        GetSubmissionDto result = assertDoesNotThrow(() -> submissionService.create(getCreateSubmissionDto(), getUser(false)));

        assertNotNull(saved.get());
        assertNotNull(sentToKafka.get());
        assertEquals(getProblem().getName(), saved.get().getProblem().getName());
        assertEquals(getUser(false).getUsername(), saved.get().getAddedBy().getUsername());
        assertEquals(getSubmission().getSourceCode(), saved.get().getSourceCode());
        assertEquals(getSubmission().getLanguage(), saved.get().getLanguage());
        assertEquals(SubmissionStatus.Pending, saved.get().getStatus());
        assertFalse(saved.get().getCompileSuccess());
        assertFalse(saved.get().getRunSuccess());
        assertFalse(saved.get().getAnswerSuccess());
        assertFalse(saved.get().getCompileErrorFatal());
        assertEquals("", saved.get().getCompileErrorMessage());
        assertEquals(0, saved.get().getTestResults().size());
        assertEquals(0, saved.get().getMaxCpu());
        assertEquals(0, saved.get().getMaxMemory());
        assertEquals(0, saved.get().getScore());
        assertEquals(Base64Helper.fromBase64(saved.get().getSourceCode()), result.getSourceCode(), "result dto should match the saved submission");
    }

    @Test
    public void createShouldThrowOnKafkaError() {
        when(problemRepository.findById(any())).thenReturn(Optional.of(getProblem()));
        when(submissionRepository.save(any())).thenAnswer(a -> {
            Submission arg = a.getArgument(0);
            arg.setId(1L);
            return arg;
        });
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.failedFuture(new InterruptedException()));

        assertThrows(RuntimeException.class, () -> submissionService.create(getCreateSubmissionDto(), getUser(false)));
    }

    @Test
    public void updateShouldThrowForInvalidId() {
        when(submissionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> submissionService.update(getSubmissionResult()));
    }

    @Test
    public void updateShouldSetDataProperly() {
        when(submissionRepository.findById(any())).thenReturn(Optional.of(getSubmission()));
        AtomicReference<Submission> saved = new AtomicReference<>(null);
        when(submissionRepository.save(any())).thenAnswer(a -> {
            Submission arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });
        AtomicReference<Long> testResultId = new AtomicReference<>(1L);
        when(testResultRepository.save(any())).thenAnswer(a -> {
            TestResult arg = a.getArgument(0);
            arg.setId(testResultId.getAndUpdate(i -> i + 1));
            return arg;
        });
        AvroSubmissionResult data = getSubmissionResult();

        assertDoesNotThrow(() -> submissionService.update(data));

        assertNotNull(saved.get());
        assertEquals(data.getCompileSuccess(), saved.get().getCompileSuccess());
        assertEquals(data.getRunSuccess(), saved.get().getRunSuccess());
        assertEquals(data.getAnswerSuccess(), saved.get().getAnswerSuccess());
        assertEquals(data.getCompileError().getMessage(), saved.get().getCompileErrorMessage());
        assertEquals(data.getCompileError().getFatal(), saved.get().getCompileErrorFatal());
    }

    @Test
    public void updateShouldSetTestResultsProperly() {
        when(submissionRepository.findById(any())).thenReturn(Optional.of(getSubmission()));
        AtomicReference<Submission> saved = new AtomicReference<>(null);
        when(submissionRepository.save(any())).thenAnswer(a -> {
            Submission arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });
        AtomicReference<Long> testResultId = new AtomicReference<>(1L);
        when(testResultRepository.save(any())).thenAnswer(a -> {
            TestResult arg = a.getArgument(0);
            arg.setId(testResultId.getAndUpdate(i -> i + 1));
            return arg;
        });
        AvroSubmissionResult data = getSubmissionResult();

        assertDoesNotThrow(() -> submissionService.update(data));

        assertNotNull(saved.get());
        assertEquals(3L, testResultId.get(), "Two test results should be saved");
        assertEquals(2, saved.get().getTestResults().size(), "Two test results should be added to submission");
        AvroTestCaseResult test1 = data.getTestResults().getFirst();
        TestResult testResult1 = saved.get().getTestResults().getFirst();
        assertAll(() -> assertEquals(test1.getOutput(), testResult1.getOutput()),
                () -> assertEquals(test1.getScore(), testResult1.getScore()),
                () -> assertEquals(test1.getErrorMessage(), testResult1.getError()),
                () -> assertEquals(SubmissionStatus.Success, testResult1.getResult()),
                () -> assertEquals(test1.getJudgerResult().getCpuTime(), testResult1.getCpuTime()),
                () -> assertEquals(test1.getJudgerResult().getRealTime(), testResult1.getRealTime()),
                () -> assertEquals(test1.getJudgerResult().getMemory(), testResult1.getMemory()),
                () -> assertEquals(test1.getJudgerResult().getSignal(), testResult1.getSignal()),
                () -> assertEquals(test1.getJudgerResult().getExitCode(), testResult1.getExitCode())
        );
        AvroTestCaseResult test2 = data.getTestResults().getLast();
        TestResult testResult2 = saved.get().getTestResults().getLast();
        assertAll(() -> assertEquals(test2.getOutput(), testResult2.getOutput()),
                () -> assertEquals(test2.getScore(), testResult2.getScore()),
                () -> assertEquals(test2.getErrorMessage(), testResult2.getError()),
                () -> assertEquals(SubmissionStatus.WrongAnswer, testResult2.getResult()),
                () -> assertEquals(test2.getJudgerResult().getCpuTime(), testResult2.getCpuTime()),
                () -> assertEquals(test2.getJudgerResult().getRealTime(), testResult2.getRealTime()),
                () -> assertEquals(test2.getJudgerResult().getMemory(), testResult2.getMemory()),
                () -> assertEquals(test2.getJudgerResult().getSignal(), testResult2.getSignal()),
                () -> assertEquals(test2.getJudgerResult().getExitCode(), testResult2.getExitCode())
        );
    }

    @Test
    public void updateShouldCalculateStatistics() {
        when(submissionRepository.findById(any())).thenReturn(Optional.of(getSubmission()));
        AtomicReference<Submission> saved = new AtomicReference<>(null);
        when(submissionRepository.save(any())).thenAnswer(a -> {
            Submission arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });
        AtomicReference<Long> testResultId = new AtomicReference<>(1L);
        when(testResultRepository.save(any())).thenAnswer(a -> {
            TestResult arg = a.getArgument(0);
            arg.setId(testResultId.getAndUpdate(i -> i + 1));
            return arg;
        });
        AvroSubmissionResult data = getSubmissionResult();

        assertDoesNotThrow(() -> submissionService.update(data));

        assertNotNull(saved.get());
        assertEquals(data.getTestResults().getFirst().getJudgerResult().getCpuTime(), saved.get().getMaxCpu());
        assertEquals(data.getTestResults().getFirst().getJudgerResult().getMemory(), saved.get().getMaxMemory());
        assertEquals(data.getTestResults().getFirst().getScore(), saved.get().getScore());
    }

    @Test
    public void updateShouldSetStatusProperly() {
        when(submissionRepository.findById(any())).thenAnswer(a -> Optional.of(getSubmission()));
        AtomicReference<Submission> saved = new AtomicReference<>(null);
        when(submissionRepository.save(any())).thenAnswer(a -> {
            Submission arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });
        AtomicReference<Long> testResultId = new AtomicReference<>(1L);
        when(testResultRepository.save(any())).thenAnswer(a -> {
            TestResult arg = a.getArgument(0);
            arg.setId(testResultId.getAndUpdate(i -> i + 1));
            return arg;
        });

        AvroSubmissionResult data1 = getSubmissionResult();
        data1.setCompileSuccess(false);
        assertDoesNotThrow(() -> submissionService.update(data1));

        assertNotNull(saved.get());
        assertEquals(SubmissionStatus.CompileError, saved.get().getStatus());

        AvroSubmissionResult data2 = getSubmissionResult();
        data2.setRunSuccess(false);
        assertDoesNotThrow(() -> submissionService.update(data2));

        assertNotNull(saved.get());
        assertEquals(SubmissionStatus.RuntimeError, saved.get().getStatus());


        AvroSubmissionResult data3 = getSubmissionResult();
        data3.setAnswerSuccess(true);
        assertDoesNotThrow(() -> submissionService.update(data3));

        assertNotNull(saved.get());
        assertEquals(SubmissionStatus.Success, saved.get().getStatus());

        AvroSubmissionResult data4 = getSubmissionResult();
        data4.setAnswerSuccess(false);
        assertDoesNotThrow(() -> submissionService.update(data4));

        assertNotNull(saved.get());
        assertEquals(SubmissionStatus.PartiallyAccepted, saved.get().getStatus());
    }

    @Test
    public void updateShouldUpdateProblem() {
        when(submissionRepository.findById(any())).thenAnswer(a -> Optional.of(getSubmission()));
        AtomicReference<Problem> saved = new AtomicReference<>(null);
        when(problemRepository.save(any())).thenAnswer(a -> {
            Problem arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });
        when(submissionRepository.save(any())).thenAnswer(a -> a.<Submission>getArgument(0));
        AtomicReference<Long> testResultId = new AtomicReference<>(1L);
        when(testResultRepository.save(any())).thenAnswer(a -> {
            TestResult arg = a.getArgument(0);
            arg.setId(testResultId.getAndUpdate(i -> i + 1));
            return arg;
        });

        AvroSubmissionResult data1 = getSubmissionResult();
        data1.setCompileSuccess(false);
        assertDoesNotThrow(() -> submissionService.update(data1));

        assertNotNull(saved.get());
        assertEquals(1, saved.get().getSubmissionCount());
        assertEquals(1, saved.get().getCompileErrors());

        AvroSubmissionResult data2 = getSubmissionResult();
        data2.setRunSuccess(false);
        assertDoesNotThrow(() -> submissionService.update(data2));

        assertNotNull(saved.get());
        assertEquals(1, saved.get().getSubmissionCount());
        assertEquals(1, saved.get().getRuntimeErrors());


        AvroSubmissionResult data3 = getSubmissionResult();
        data3.setAnswerSuccess(true);
        assertDoesNotThrow(() -> submissionService.update(data3));

        assertNotNull(saved.get());
        assertEquals(1, saved.get().getSubmissionCount());
        assertEquals(1, saved.get().getAcceptedSubmissions());

        AvroSubmissionResult data4 = getSubmissionResult();
        data4.setAnswerSuccess(false);
        assertDoesNotThrow(() -> submissionService.update(data4));

        assertNotNull(saved.get());
        assertEquals(1, saved.get().getSubmissionCount());
        assertEquals(1, saved.get().getPartiallyAccepted());
    }
}
