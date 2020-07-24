package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.dto.StatisticsDto;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsGenerationService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsService;
import com.test.digitalzone.reactiveregistration.services.interfaces.TablesService;
import lombok.RequiredArgsConstructor;
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
    private LocalDateTime dayStart;
    private LocalDateTime dayEnd;

    @PostConstruct
    public void init() {
        LocalDateTime localDateTime = LocalDateTime.now();
        dayEnd = localDateTime.toLocalDate().atTime(LocalTime.MAX);
        dayStart = localDateTime.toLocalDate().atTime(LocalTime.MIN);

        System.err.println(dayStart);
        System.err.println(dayEnd);
    }

    @Override
    public StatisticsDto getCurrentDayStatistics() {
        List<String> tableNames = tablesService.getAllTables();
        List<Future<Long>> results = new ArrayList<>();
        for (String name : tableNames) {
            results.add(statisticsGenerationService.getCurrentAmountFromTableByStartAndTheEnd(dayStart, dayEnd, name));
        }
        return StatisticsDto.builder().count(results.stream().map(x -> {
            try {
                return x.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        })
                .reduce(0L, Long::sum)).unique(redisService.getTodayUniqueNumber()).build();
    }
}
