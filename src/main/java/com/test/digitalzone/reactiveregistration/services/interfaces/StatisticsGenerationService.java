package com.test.digitalzone.reactiveregistration.services.interfaces;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

public interface StatisticsGenerationService {
    Future<Long> getCurrentAmountFromTableByStartAndTheEnd(LocalDateTime start, LocalDateTime end, String tableName);
}
