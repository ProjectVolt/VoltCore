package io.github.capure.voltcore.service;

import io.github.capure.schema.TestCaseEvent;
import io.github.capure.schema.TestCaseEventType;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.dto.admin.PutTestCaseDto;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.TestCase;
import io.github.capure.voltcore.repository.TestCaseRepository;
import io.github.capure.voltcore.util.Base64Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    public void editShouldWorkForValidData() {
        PutTestCaseDto data = new PutTestCaseDto();
        data.setId(1L);
        data.setName("Hello");
        data.setInput("In");
        data.setOutput("Out");
        data.setMaxScore(5);
        AtomicReference<TestCase> saved = new AtomicReference<>();
        AtomicReference<Integer> kafkaCalled = new AtomicReference<>(0);
        when(testCaseRepository.existsById(1L)).thenReturn(true);
        when(testCaseRepository.save(any())).thenAnswer(a -> {
            TestCase arg = a.getArgument(0);
            saved.set(arg);
            return arg;
        });
        when(kafkaTemplate.send(any(), any(), any())).thenAnswer(a -> {
            TestCaseEvent arg = a.getArgument(2);
            if (kafkaCalled.get() == 0) {
                assertEquals(TestCaseEventType.DELETE, arg.getType());
            } else {
                assertEquals(TestCaseEventType.ADD, arg.getType());
            }
            kafkaCalled.updateAndGet(c -> c + 1);
            return CompletableFuture.completedFuture(null);
        });

        TestCase result = assertDoesNotThrow(() -> testCaseService.edit(data, getTestCase().getProblem()));

        assertEquals(saved.get(), result);
        assertEquals(2, kafkaCalled.get());
        assertEquals(data.getInput(), Base64Helper.fromBase64(result.getInput()));
    }

    @Test
    public void editShouldThrowForKafkaError() {
        PutTestCaseDto data = new PutTestCaseDto();
        data.setId(1L);
        data.setName("Hello");
        data.setInput("In");
        data.setOutput("Out");
        data.setMaxScore(5);
        when(testCaseRepository.existsById(1L)).thenReturn(true);
        when(testCaseRepository.save(any())).thenAnswer(a -> a.<TestCase>getArgument(0));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.failedFuture(new InterruptedException()));

        assertThrows(RuntimeException.class, () -> testCaseService.edit(data, getTestCase().getProblem()));
    }

    @Test
    public void deleteShouldWorkForValidData() {
        TestCase init = getTestCase();
        AtomicReference<Boolean> deleteCalled = new AtomicReference<>(false);
        AtomicReference<Boolean> kafkaCalled = new AtomicReference<>(false);
        Mockito.doAnswer(a -> {
            deleteCalled.set(true);
            return null;
        }).when(testCaseRepository).delete(any());
        when(kafkaTemplate.send(any(), any(), any())).thenAnswer(a -> {
            TestCaseEvent data = a.getArgument(2);
            assertEquals(TestCaseEventType.DELETE, data.getType());
            kafkaCalled.set(true);
            return CompletableFuture.completedFuture(null);
        });

        assertDoesNotThrow(() -> testCaseService.delete(init));

        assertTrue(deleteCalled.get());
        assertTrue(kafkaCalled.get());
    }

    @Test
    public void deleteShouldThrowRuntimeExceptionForKafkaException() {
        TestCase init = getTestCase();
        AtomicReference<Boolean> deleteCalled = new AtomicReference<>(false);
        Mockito.doAnswer(a -> {
            deleteCalled.set(true);
            return null;
        }).when(testCaseRepository).delete(any());
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.failedFuture(new InterruptedException()));

        assertThrows(RuntimeException.class, () -> testCaseService.delete(init));

        assertTrue(deleteCalled.get());
    }
}
