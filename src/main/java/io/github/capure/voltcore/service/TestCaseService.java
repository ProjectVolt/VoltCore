package io.github.capure.voltcore.service;

import io.github.capure.schema.TestCaseEvent;
import io.github.capure.schema.TestCaseEventType;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.dto.admin.PutTestCaseDto;
import io.github.capure.voltcore.exception.InvalidIdRuntimeException;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.repository.TestCaseRepository;
import io.github.capure.voltcore.util.Base64Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class TestCaseService {
    @Autowired
    TestCaseRepository testCaseRepository;

    @Autowired
    private KafkaTemplate<String, TestCaseEvent> kafkaTemplate;

    @Transactional(value = "kafkaTransactionManager", rollbackFor = {ExecutionException.class, InterruptedException.class})
    public void sendTestCaseEditEventToKafka(TestCaseEvent data) throws ExecutionException, InterruptedException {
        log.info("Sending to kafka");
        kafkaTemplate.send("test_case_edit_events", data.getDetails().getId().toString(), data).get();
        log.info("Sent to kafka");
    }

    @Transactional(value = "kafkaTransactionManager", rollbackFor = {ExecutionException.class, InterruptedException.class})
    public void sendTestCaseUpdate(TestCase data) throws ExecutionException, InterruptedException {
        log.info("Sending to kafka");
        kafkaTemplate.send("test_case_edit_events", data.getId().toString(), new TestCaseEvent(TestCaseEventType.DELETE, data.toAvro())).get();
        kafkaTemplate.send("test_case_edit_events", data.getId().toString(), new TestCaseEvent(TestCaseEventType.ADD, data.toAvro())).get();
        log.info("Sent to kafka");
    }

    @Transactional("transactionManager")
    public TestCase create(CreateTestCaseDto data, Problem problem) {
        log.info("Attempting to add a new test case - problem_id: {} name: {}", problem.getId(), data.getName());

        TestCase testCase = new TestCase(null,
                problem,
                data.getName(),
                Base64Helper.toBase64(data.getInput()),
                Base64Helper.toBase64(data.getOutput()),
                data.getMaxScore());

        log.info("Saving to database");
        TestCase result = testCaseRepository.save(testCase);
        log.info("Saved successfully");

        try {
            sendTestCaseEditEventToKafka(new TestCaseEvent(TestCaseEventType.ADD, result.toAvro()));
        } catch (Exception e) {
            log.error("Couldn't send to kafka");
            throw new RuntimeException(e);
        }

        return result;
    }

    @Transactional("transactionManager")
    public TestCase edit(PutTestCaseDto data, Problem problem) {
        log.info("Attempting to edit a test case - problem_id: {} name: {}", problem.getId(), data.getName());

        if (!testCaseRepository.existsById(data.getId())) throw new InvalidIdRuntimeException();

        TestCase testCase = new TestCase(data.getId(),
                problem,
                data.getName(),
                Base64Helper.toBase64(data.getInput()),
                Base64Helper.toBase64(data.getOutput()),
                data.getMaxScore());

        log.info("Saving to database");
        TestCase result = testCaseRepository.save(testCase);
        log.info("Saved successfully");

        try {
            sendTestCaseUpdate(result);
        } catch (Exception e) {
            log.error("Couldn't send to kafka");
            throw new RuntimeException(e);
        }

        return result;
    }

    @Transactional("transactionManager")
    public void delete(TestCase data) {
        log.info("Deleting test case {}", data.getId());
        log.info("Deleting from db");
        testCaseRepository.delete(data);
        log.info("Deleting from dynamo");
        try {
            sendTestCaseEditEventToKafka(new TestCaseEvent(TestCaseEventType.DELETE, data.toAvro()));
        } catch (Exception e) {
            log.error("Couldn't send to kafka");
            throw new RuntimeException(e);
        }
        log.info("Successfully deleted test case {}", data.getId());
    }
}
