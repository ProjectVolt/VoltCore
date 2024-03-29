package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.repository.TestCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestCaseService.class, TestCaseRepository.class})
public class TestCaseServiceTest {
    @Autowired
    TestCaseService testCaseService;

    @MockBean
    TestCaseRepository testCaseRepository;

    private TestCase getTestCase() {
        Problem problem = new Problem();
        problem.setId(1L);
        return new TestCase(1L, problem, "test", "input", "output", 10);
    }

    @Test
    public void createShouldNotThrowAndReturnNewTestCaseForValidData() {
        TestCase init = getTestCase();
        when(testCaseRepository.save(any())).thenReturn(init);
        CreateTestCaseDto data = new CreateTestCaseDto(init.getName(), init.getInput(), init.getOutput(), init.getMaxScore());

        TestCase result = assertDoesNotThrow(() -> testCaseService.create(data, init.getProblem()));

        assertEquals(init.getName(), result.getName());
    }
}
