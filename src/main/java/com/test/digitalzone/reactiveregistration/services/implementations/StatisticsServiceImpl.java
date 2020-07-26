package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.dto.StatisticsDto;
import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.agkn.hll.HLL;
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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final RedisService redisService;
    private final TablesService tablesService;
    private final StatisticsGenerationService statisticsGenerationService;
    //used to calculate the optimal size for bloom filter
    private static final double LN09 = -0.105305157;


    private LocalDateTime dayStart;

    private LocalDateTime dayEnd;

    @Override
    public void setDayStart(LocalDateTime start) {
        this.dayStart = start;
    }

    @Override
    public void setDayEnd(LocalDateTime end) {
        this.dayEnd = end;
    }

    @PostConstruct
    public void init() {
        LocalDateTime localDateTime = LocalDateTime.now();
        dayEnd = localDateTime.toLocalDate().atTime(LocalTime.MAX);
        dayStart = localDateTime.toLocalDate().atTime(LocalTime.MIN);
    }

    @Override
    public StatisticsDto getCurrentDayStatistics() {
        Function<? super Future<Long>, ? extends Long> extractor = future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        };
        Future<List<String>> tableNames = tablesService.getAllTables();
        List<Future<Long>> results = new ArrayList<>();
        try {
            for (String name : tableNames.get()) {
                results.add(statisticsGenerationService.getCurrentAmountFromTableByStartAndTheEnd(dayStart, dayEnd, name));
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
        return StatisticsDto.builder().count(results.stream().map(extractor).reduce(0L, Long::sum)).unique(redisService.getTodayUniqueNumber()).build();
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<StatisticsDto> getStatisticsByDates(LocalDateTime start, LocalDateTime end) throws ExecutionException, InterruptedException {
        Future<List<String>> tableNames = tablesService.getAllTables();

        List<String> names = tableNames.get();
        List<Future<List<ViewEvent>>> futureListOfEvents = new ArrayList<>(names.size());

        for (int i = 0; i < names.size(); i++) {
            futureListOfEvents.add(i, statisticsGenerationService.getAllEventsByTableBetween(start, end, names.get(i)));
        }

        List<List<ViewEvent>> listOfResultEventsFromDatabase = new ArrayList<>(names.size());
        List<Future<Long>> listToCheckMethodsToComplete = new ArrayList<>(names.size());

        List<ViewEvent> resultEvents = new ArrayList<>();
        //в итоге было принято решения считать уникальных пользователей локально через hll, поскольку отправка всех необходимых пользователей в redis hll занимала катастрофические 5сек.
        HLL hll = getHLL(15, 4);
        int current = 0;
        for (Future<List<ViewEvent>> futureListOfEvent : futureListOfEvents) {
            resultEvents = futureListOfEvent.get();
            if (resultEvents.size() == 0) continue;
            listOfResultEventsFromDatabase.add(current, resultEvents);
            listToCheckMethodsToComplete.add(statisticsGenerationService.addAllEventsToHLL(resultEvents, hll));
            current++;
        }

        for (int i = 0; i < current; i++) {
            listToCheckMethodsToComplete.get(i).get();
        }

        Long uniqueViewersNumber = hll.cardinality();
        int size = (int) Math.round(-uniqueViewersNumber / LN09);

        List<Future<byte[]>> futureResultArraysToCall = new ArrayList<>(listToCheckMethodsToComplete.size());

        for (int i = 0; i < current; i++) {
            futureResultArraysToCall.add(i, statisticsGenerationService.getDummyBloomFilter(size, listOfResultEventsFromDatabase.get(i)));
        }

        List<byte[]> dummyBloomFiltersList = new ArrayList<>();
        for (int i = 0; i < current; i++) {
            dummyBloomFiltersList.add(i, futureResultArraysToCall.get(i).get());
        }

        Future<Long> countAll = statisticsGenerationService.calculateAllViews(listOfResultEventsFromDatabase);
        Future<Long> regularUsers = statisticsGenerationService.calculateResultForRegularUsers(dummyBloomFiltersList);

        return AsyncResult.forValue(StatisticsDto.builder().unique(uniqueViewersNumber).count(countAll.get()).regularUsers(regularUsers.get()).build());
    }


    private HLL getHLL(int buckets, int i) {
        return new HLL(buckets, i);
    }
}
