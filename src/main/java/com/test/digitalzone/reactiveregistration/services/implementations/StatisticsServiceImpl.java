package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.dto.StatisticsDto;
import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final RedisService redisService;
    private final TablesService tablesService;
    private final StatisticsGenerationService statisticsGenerationService;
    private final UniqueViewStatisticsService uniqueViewStatisticsService;
    //used to calculate the optimal size for bloom filter
    private static final double LN09 = -0.105305157;

    private LocalDateTime dayStart;
    private LocalDateTime dayEnd;

    @PostConstruct
    public void init() {
        LocalDateTime localDateTime = LocalDateTime.now();
        dayEnd = localDateTime.toLocalDate().atTime(LocalTime.MAX);
        dayStart = localDateTime.toLocalDate().atTime(LocalTime.MIN);
    }

    @Override
    public StatisticsDto getCurrentDayStatistics() {
        Future<List<String>> tableNames = tablesService.getAllTables();
        List<Future<Long>> results = new ArrayList<>();
        try {
            for (String name : tableNames.get()) {
                results.add(statisticsGenerationService.getCurrentAmountFromTableByStartAndTheEnd(dayStart, dayEnd, name));
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return StatisticsDto.builder().count(results.stream().map(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }).reduce(0L, Long::sum)).unique(redisService.getTodayUniqueNumber()).build();
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<StatisticsDto> getStatisticsByDates(LocalDateTime start, LocalDateTime end) {
        Future<List<String>> tableNames = tablesService.getAllTables();
        Long x = System.currentTimeMillis();
        Future<Pair<Boolean, String>> resultFromIndexService = uniqueViewStatisticsService.getNameOfMergedUniqueIndex(start, end);
        Pair<Boolean, String> caseAndNameForIndex;
        try {
            List<String> names = tableNames.get();
            List<Future<List<ViewEvent>>> events = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                events.set(i, statisticsGenerationService.getAllEventsByTableBetween(start, end, names.get(i)));
            }

            List<List<ViewEvent>> resultEventsFromDatabase = new ArrayList<>(names.size());
            caseAndNameForIndex = resultFromIndexService.get();

            List<Future<Boolean>> listToCheckVoidMethodsToComplete = new ArrayList<>(names.size());

            for (int i = 0; i < events.size(); i++) {
                resultEventsFromDatabase.set(i, events.get(i).get());
                listToCheckVoidMethodsToComplete.set(i, statisticsGenerationService.addAllEventsToIndex(
                        caseAndNameForIndex, resultEventsFromDatabase.get(i), start, end, names.get(i)));
            }

            for (Future<Boolean> checkVariable : listToCheckVoidMethodsToComplete) {
                checkVariable.get();
            }
            Long uniqueViewersNumber = redisService.getUniqueNumberForTag(caseAndNameForIndex.getSecond());
            int size = (int) Math.round(-uniqueViewersNumber / LN09);
            List<Future<byte[]>> futureResultArraysToCall = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                futureResultArraysToCall.set(i, statisticsGenerationService.getResultDummyBloomFilter(size, resultEventsFromDatabase.get(i)));
            }
            List<byte[]> resultDummyBloomFilters = new ArrayList<>(names.size());
            for(int i = 0; i < names.size(); i++){
                resultDummyBloomFilters.set(i, futureResultArraysToCall.get(i).get());
            }
            Future<Long> countAll = statisticsGenerationService.calculateAllViews(resultEventsFromDatabase);
            Future<Long> regularUsers = statisticsGenerationService.calculateResultForRegularUsers(resultDummyBloomFilters);
            Long y = System.currentTimeMillis();
            System.out.println(y - x);
            return AsyncResult.forValue(StatisticsDto.builder().unique(uniqueViewersNumber).count(countAll.get()).regularUsers(regularUsers.get())
                    .build());
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
