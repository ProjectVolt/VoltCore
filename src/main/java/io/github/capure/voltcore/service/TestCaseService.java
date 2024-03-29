package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.repository.TestCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TestCaseService {
    @Autowired
    TestCaseRepository testCaseRepository;

    @Transactional
    public TestCase create(CreateTestCaseDto data, Problem problem) {
        log.info("Attempting to add a new test case - problem_id: {} name: {}", problem.getId(), data.getName());

        TestCase testCase = new TestCase(null,
                problem,
                data.getName(),
                data.getInput(),
                data.getOutput(),
                data.getMaxScore());

        log.info("Saving to database");
        TestCase result = testCaseRepository.save(testCase);
        log.info("Saved successfully");

        // produce to kafka, for dynamo to replicate the data

        return result;
    }
}
