package io.github.capure.voltcore.service;

import io.github.capure.schema.TestCaseEvent;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.repository.TestCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestCaseService.class})
public class TestCaseServiceTest {
    @Autowired
    TestCaseService testCaseService;

    @MockBean
    TestCaseRepository testCaseRepository;

    @MockBean
    KafkaTemplate<String, TestCaseEvent> kafkaTemplate;

    private TestCase getTestCase() {
        Problem problem = new Problem();
        problem.setId(1L);
        return new TestCase(1L, problem, "test", "input", "output", 10);
    }

    @Test
    public void createShouldNotThrowAndReturnNewTestCaseForValidData() {
        TestCase init = getTestCase();
        when(testCaseRepository.save(any())).thenAnswer(a -> {
            TestCase t = a.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        CreateTestCaseDto data = new CreateTestCaseDto(init.getName(), init.getInput(), init.getOutput(), init.getMaxScore());

        TestCase result = assertDoesNotThrow(() -> testCaseService.create(data, init.getProblem()));

        assertEquals(init.getName(), result.getName());
        assertEquals(Base64.getEncoder().encodeToString(init.getOutput().getBytes()), result.getOutput());
    }
}
