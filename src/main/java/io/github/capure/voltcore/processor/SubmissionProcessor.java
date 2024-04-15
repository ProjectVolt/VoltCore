package io.github.capure.voltcore.processor;

import io.github.capure.schema.AvroSubmissionResult;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class SubmissionProcessor {
    @Autowired
    private SubmissionService submissionService;

    @KafkaListener(topics = "submission_results", groupId = "core")
    @Transactional(value = "kafkaTransactionManager", rollbackFor = {InvalidIdException.class})
    public void process(ConsumerRecord<String, AvroSubmissionResult> data) throws InvalidIdException {
        log.info("Processing submission {}", data.value().getSubmissionId());
        submissionService.update(data.value());
        log.info("Submission {} processed successfully", data.value().getSubmissionId());
    }
}
