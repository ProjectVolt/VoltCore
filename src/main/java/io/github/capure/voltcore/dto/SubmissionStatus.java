package io.github.capure.voltcore.dto;

import io.github.capure.schema.AvroJudgerResultCode;

public abstract class SubmissionStatus {
    public final static String CompileError = "COMPILE_ERROR";
    public final static String WrongAnswer = "WRONG_ANSWER";
    public final static String Success = "SUCCESS";
    public final static String CpuTimeLimitExceeded = "CPU_TIME_LIMIT_EXCEEDED";
    public final static String RealTimeLimitExceeded = "REAL_TIME_LIMIT_EXCEEDED";
    public final static String MemoryLimitExceeded = "MEMORY_LIMIT_EXCEEDED";
    public final static String RuntimeError = "RUNTIME_ERROR";
    public final static String SystemError = "SYSTEM_ERROR";
    public final static String PartiallyAccepted = "PARTIALLY_ACCEPTED";
    public final static String Pending = "PENDING";

    public static String fromAvro(AvroJudgerResultCode data) {
        return data.name().split("RESULT_")[1];
    }
}
