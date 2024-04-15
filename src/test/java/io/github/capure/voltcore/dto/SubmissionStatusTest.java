package io.github.capure.voltcore.dto;

import io.github.capure.schema.AvroJudgerResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubmissionStatusTest {
    @Test
    public void toAvroShouldReturnValidStatus() {
        assertEquals(SubmissionStatus.WrongAnswer, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_WRONG_ANSWER));
        assertEquals(SubmissionStatus.Success, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_SUCCESS));
        assertEquals(SubmissionStatus.CpuTimeLimitExceeded, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_CPU_TIME_LIMIT_EXCEEDED));
        assertEquals(SubmissionStatus.RealTimeLimitExceeded, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_REAL_TIME_LIMIT_EXCEEDED));
        assertEquals(SubmissionStatus.MemoryLimitExceeded, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_MEMORY_LIMIT_EXCEEDED));
        assertEquals(SubmissionStatus.RuntimeError, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_RUNTIME_ERROR));
        assertEquals(SubmissionStatus.SystemError, SubmissionStatus.fromAvro(AvroJudgerResultCode.RESULT_SYSTEM_ERROR));
    }
}
