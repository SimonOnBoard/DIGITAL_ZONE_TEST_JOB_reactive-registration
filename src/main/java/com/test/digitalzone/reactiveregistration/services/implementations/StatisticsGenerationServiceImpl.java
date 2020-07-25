package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventFindQueuesRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.swing.text.View;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.Adler32;

@Service
@RequiredArgsConstructor
public class StatisticsGenerationServiceImpl implements StatisticsGenerationService {
    private final ViewEventFindQueuesRepository viewEventFindQueuesRepository;
    private final RedisService redisService;

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> getCurrentAmountFromTableByStartAndTheEnd(LocalDateTime start, LocalDateTime end, String tableName) {
        if (!viewEventFindQueuesRepository.checkIfTableExists(tableName)) return AsyncResult.forValue(1L);
        Long startId = viewEventFindQueuesRepository.findIdByLowerBound(tableName, start);
        Long endId = viewEventFindQueuesRepository.findIdByUpperBound(tableName, end);
        if (endId == 0) return AsyncResult.forValue(0L);
        return AsyncResult.forValue(endId - startId + 1);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<List<ViewEvent>> getAllEventsByTableBetween(LocalDateTime start, LocalDateTime end, String tableName) {
        return AsyncResult.forValue(viewEventFindQueuesRepository.findAllFromTableBetween(start, end, tableName));
    }


    //true = разные дни
    //false = один и тот же день
    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Boolean> addAllEventsToIndex(Pair<Boolean, String> caseAndIndexName, List<ViewEvent> events, LocalDateTime start, LocalDateTime end, String tableName) {
        if (caseAndIndexName.getFirst()) {
            Long maxFirstDayId = viewEventFindQueuesRepository.findIdByUpperBound(tableName, start.toLocalDate().atTime(LocalTime.MAX));
            Long minLastDayId = viewEventFindQueuesRepository.findIdByLowerBound(tableName, end.toLocalDate().atTime(LocalTime.MIN));

            for (ViewEvent viewEvent : events) {
                if (viewEvent.getId() <= maxFirstDayId | (minLastDayId != null && viewEvent.getId() >= minLastDayId)) {
                    redisService.publishUserId(viewEvent.getUserId(), caseAndIndexName.getSecond());
                }
            }
        } else {
            for (ViewEvent viewEvent : events) {
                redisService.publishUserId(viewEvent.getUserId(), caseAndIndexName.getSecond());
            }
        }
        return AsyncResult.forValue(true);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    //сознательно использую одну хеш функцию для равномерного размытия значений по блум фильтрам, здесь мне важно просто существование уникального пользователя
    public Future<byte[]> getResultDummyBloomFilter(int size, List<ViewEvent> viewEvents) {
        byte[] resultArray = new byte[size];
        Adler32 adler23 = new Adler32();
        for (ViewEvent viewEvent : viewEvents) {
            adler23.update(viewEvent.getUserId().getBytes(), 0, viewEvent.getUserId().length());
            resultArray[(int) (adler23.getValue() % size)] += 1;
            adler23.reset();
        }
        return AsyncResult.forValue(resultArray);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> calculateResultForRegularUsers(List<byte[]> resultBloomFilters) {
        long resultCounter = 0;
        int localCounter = 0;
        for(int i = 0; i < resultBloomFilters.get(0).length; i++){
            localCounter = 0;
            for(byte[] hashArray: resultBloomFilters){
                if(hashArray[i] != 0) localCounter++;
                if(localCounter >= 10){
                    resultCounter++;
                    break;
                }
            }
        }
        return AsyncResult.forValue(resultCounter);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> calculateAllViews(List<List<ViewEvent>> listOfEvents) {
        long result = 0;
        for (List<ViewEvent> viewEvents : listOfEvents) {
            result += viewEvents.get(viewEvents.size() - 1).getId() - viewEvents.get(0).getId();
        }
        return AsyncResult.forValue(result);
    }

}
