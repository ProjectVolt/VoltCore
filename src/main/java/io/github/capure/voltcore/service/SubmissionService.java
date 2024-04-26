package io.github.capure.voltcore.service;

import io.github.capure.schema.AvroSubmission;
import io.github.capure.schema.AvroSubmissionResult;
import io.github.capure.voltcore.dto.CreateSubmissionDto;
import io.github.capure.voltcore.dto.GetSubmissionDto;
import io.github.capure.voltcore.dto.SubmissionStatus;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.ProblemNotVisibleException;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.Submission;
import io.github.capure.voltcore.model.TestResult;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.repository.ProblemRepository;
import io.github.capure.voltcore.repository.SubmissionRepository;
import io.github.capure.voltcore.repository.TestResultRepository;
import io.github.capure.voltcore.util.Base64Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class SubmissionService {
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private KafkaTemplate<String, AvroSubmission> kafkaTemplate;

    @Transactional(value = "kafkaTransactionManager", rollbackFor = {ExecutionException.class, InterruptedException.class})
    public void sendSubmissionToKafka(AvroSubmission data) throws ExecutionException, InterruptedException {
        log.info("Sending to kafka");
        kafkaTemplate.send("submissions", data.getProblemId().toString(), data).get();
        log.info("Sent to kafka");
    }

    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class})
    public GetSubmissionDto get(Long id, User user) throws InvalidIdException {
        Submission submission = submissionRepository.findById(id).orElseThrow(InvalidIdException::new);
        boolean showCode = Objects.equals(submission.getAddedBy().getId(), user.getId()) || Objects.equals(user.getRole(), "ROLE_ADMIN");
        return new GetSubmissionDto(submission, showCode);
    }

    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class})
    public List<GetSubmissionDto> getByUserAndProblemId(User user, Long problemId, Integer limit) throws InvalidIdException {
        List<Submission> submissions = submissionRepository.findByAddedByAndProblem_IdOrderByCreatedOnDesc(user, problemId, PageRequest.of(0, limit));
        return submissions.parallelStream().map(submission -> {
            boolean showCode = Objects.equals(submission.getAddedBy().getId(), user.getId()) || Objects.equals(user.getRole(), "ROLE_ADMIN");
            return new GetSubmissionDto(submission, showCode);
        }).toList();
    }

    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class, ProblemNotVisibleException.class})
    public GetSubmissionDto create(CreateSubmissionDto data, User user) throws InvalidIdException, ProblemNotVisibleException {
        Problem problem = problemRepository.findById(data.getProblemId()).orElseThrow(InvalidIdException::new);
        if (!problem.isVisible()) throw new ProblemNotVisibleException();
        log.info("Creating new submission - problem_id: {} - user: {}", data.getProblemId(), user.getId());
        Submission submission = new Submission(null,
                problem,
                null,
                user,
                Base64Helper.toBase64(data.getSourceCode()),
                data.getLanguage(),
                SubmissionStatus.Pending,
                false,
                false,
                false,
                "",
                false,
                List.of(),
                0,
                0,
                0);

        log.info("Saving to db");
        Submission result = submissionRepository.save(submission);
        log.info("Saved successfully");

        try {
            sendSubmissionToKafka(submission.toAvro());
        } catch (Exception e) {
            log.error("Couldn't send to kafka");
            throw new RuntimeException();
        }

        return new GetSubmissionDto(result, true);
    }

    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class})
    public void update(AvroSubmissionResult data) throws InvalidIdException {
        Submission submission = submissionRepository.findById(data.getSubmissionId()).orElseThrow(InvalidIdException::new);

        log.info("Updating submission {}", submission.getId());

        log.info("Setting data");
        submission.setCompileSuccess(data.getCompileSuccess());
        submission.setRunSuccess(data.getRunSuccess());
        submission.setAnswerSuccess(data.getAnswerSuccess());
        submission.setCompileErrorMessage(data.getCompileError().getMessage().toString());
        submission.setCompileErrorFatal(data.getCompileError().getFatal());

        log.info("Adding test results");
        submission.setTestResults(new ArrayList<>(data.getTestResults().stream().map(t -> new TestResult(t, submission)).map(t -> testResultRepository.save(t)).toList()));

        log.info("Calculating statistics");
        submission.setMaxCpu(submission.getTestResults().stream().map(TestResult::getCpuTime).max(Comparator.naturalOrder()).orElse(0));
        submission.setMaxMemory(submission.getTestResults().stream().map(TestResult::getMemory).max(Comparator.naturalOrder()).orElse(0));
        submission.setScore(submission.getTestResults().stream().map(TestResult::getScore).mapToInt(i -> i).sum());

        log.info("Setting status");
        if (!submission.getCompileSuccess()) {
            submission.setStatus(SubmissionStatus.CompileError);
        } else if (!submission.getRunSuccess()) {
            submission.setStatus(SubmissionStatus.RuntimeError);
            List<String> results = submission.getTestResults().stream().map(TestResult::getResult).toList();
            for (String status : results) {
                if (!status.equals(SubmissionStatus.Success) && !status.equals(SubmissionStatus.WrongAnswer)) {
                    submission.setStatus(status);
                    break;
                }
            }
        } else if (submission.getAnswerSuccess()) {
            submission.setStatus(SubmissionStatus.Success);
        } else {
            submission.setStatus(SubmissionStatus.PartiallyAccepted);
        }

        if (submission.getStatus().equals(SubmissionStatus.SystemError)) {
            log.error("Encountered system error - submission_id: {}", submission.getId());
        }

        log.info("Updating problem statistics");
        Problem problem = submission.getProblem();
        if (!submission.getStatus().equals(SubmissionStatus.SystemError)) {
            problem.setSubmissionCount(problem.getSubmissionCount() + 1);
        }
        switch (submission.getStatus()) {
            case SubmissionStatus.Success -> problem.setAcceptedSubmissions(problem.getAcceptedSubmissions() + 1);
            case SubmissionStatus.WrongAnswer -> problem.setWrongSubmissions(problem.getWrongSubmissions() + 1);
            case SubmissionStatus.PartiallyAccepted -> problem.setPartiallyAccepted(problem.getPartiallyAccepted() + 1);
            case SubmissionStatus.RuntimeError -> problem.setRuntimeErrors(problem.getRuntimeErrors() + 1);
            case SubmissionStatus.CompileError -> problem.setCompileErrors(problem.getCompileErrors() + 1);
        }
        problemRepository.save(problem);
        log.info("Problem statistics updated successfully");

        log.info("Saving updated submission");
        submissionRepository.save(submission);
        log.info("Saved successfully");
    }
}
