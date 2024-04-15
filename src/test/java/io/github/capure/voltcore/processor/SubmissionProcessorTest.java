package io.github.capure.voltcore.processor;

import io.github.capure.schema.AvroCompileError;
import io.github.capure.schema.AvroSubmissionResult;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.service.SubmissionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SubmissionProcessor.class})
public class SubmissionProcessorTest {
    @Autowired
    private SubmissionProcessor submissionProcessor;

    @MockBean
    private SubmissionService submissionService;

    @Test
    public void processShouldCallUpdateOnSubmissionService() throws InvalidIdException {
        AtomicReference<Boolean> updateCalled = new AtomicReference<>(false);
        Mockito.doAnswer(a -> {
            updateCalled.set(true);
            return null;
        }).when(submissionService).update(any());
        AvroSubmissionResult submissionResult = new AvroSubmissionResult(1L, 1L, true, true, true, new AvroCompileError("", false), List.of());
        ConsumerRecord<String, AvroSubmissionResult> data = new ConsumerRecord<>("submisson_results", 0, 0, "", submissionResult);

        assertDoesNotThrow(() -> submissionProcessor.process(data));

        assertTrue(updateCalled.get());
    }

    @Test
    public void processShouldRethrowInvalidIdFromSubmissionService() throws InvalidIdException {
        Mockito.doThrow(InvalidIdException.class).when(submissionService).update(any());
        AvroSubmissionResult submissionResult = new AvroSubmissionResult(1L, 1L, true, true, true, new AvroCompileError("", false), List.of());
        ConsumerRecord<String, AvroSubmissionResult> data = new ConsumerRecord<>("submisson_results", 0, 0, "", submissionResult);

        assertThrows(InvalidIdException.class, () -> submissionProcessor.process(data));

    }
}
