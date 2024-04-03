package io.github.capure.voltcore.service;

import io.github.capure.schema.TestCaseEvent;
import io.github.capure.schema.TestCaseEventType;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.repository.TestCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class TestCaseService {
    @Autowired
    TestCaseRepository testCaseRepository;

    @Autowired
    private KafkaTemplate<String, TestCaseEvent> kafkaTemplate;

    private String toBase64(String in) {
        return Base64.getEncoder().encodeToString(in.getBytes());
    }

    @Transactional(value = "kafkaTransactionManager", rollbackFor = {ExecutionException.class, InterruptedException.class})
    public void sendTestCaseEditEventToKafka(TestCaseEvent data) throws ExecutionException, InterruptedException {
        log.info("Sending to kafka");
        kafkaTemplate.send("test_case_edit_events", data.getDetails().getId().toString(), data).get();
        log.info("Sent to kafka");
    }

    @Transactional("transactionManager")
    public TestCase create(CreateTestCaseDto data, Problem problem) {
        log.info("Attempting to add a new test case - problem_id: {} name: {}", problem.getId(), data.getName());

        TestCase testCase = new TestCase(null,
                problem,
                data.getName(),
                toBase64(data.getInput()),
                toBase64(data.getOutput()),
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
}
