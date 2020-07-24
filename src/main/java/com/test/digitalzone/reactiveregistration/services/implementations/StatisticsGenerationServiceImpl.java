package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventFindQueuesRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Future;


@Service
@RequiredArgsConstructor
public class StatisticsGenerationServiceImpl implements StatisticsGenerationService {
    private final ViewEventFindQueuesRepository viewEventFindQueuesRepository;

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> getCurrentAmountFromTableByStartAndTheEnd(LocalDateTime start, LocalDateTime end, String tableName) {
        Long startId = viewEventFindQueuesRepository.findIdByLowerBound(tableName, start);
        Long endId = viewEventFindQueuesRepository.findIdByUpperBound(tableName, end);
        if (endId == 0) return AsyncResult.forValue(0L);
        return AsyncResult.forValue(endId - startId + 1);
    }
}
