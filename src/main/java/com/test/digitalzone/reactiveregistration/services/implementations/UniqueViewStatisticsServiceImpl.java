package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.repositories.interfaces.RedisRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import com.test.digitalzone.reactiveregistration.services.interfaces.UniqueViewStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Future;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniqueViewStatisticsServiceImpl implements UniqueViewStatisticsService {
    private final RedisService redisService;

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Pair<Boolean, String>> getNameOfMergedUniqueIndex(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        String indexName = createIndexByDates(start, end);
        if (startDate.getDayOfMonth() == endDate.getDayOfMonth() &&
                startDate.getYear() == endDate.getYear() &&
                startDate.getMonthValue() == endDate.getMonthValue()) {
            return AsyncResult.forValue(Pair.of(false, indexName));
        }
        mergeIndexBetween(startDate, endDate, indexName);
        return AsyncResult.forValue(Pair.of(true, indexName));
    }


    private void mergeIndexBetween(LocalDate startDate, LocalDate endDate, String indexName) {
        List<LocalDate> datesBetween = getDatesBetween(startDate, endDate);
        List<String> indexes = datesBetween.stream().map(this::getIndexFromDate).collect(Collectors.toList());
        if(redisService.mergeIndexes(indexName, indexes) == null) throw new IllegalStateException("can't merge indexes");
    }

    private String getIndexFromDate(LocalDate date) {
        return  date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear();
    }

    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        startDate = startDate.plusDays(1);
        endDate = endDate.minusDays(1);
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dates.add(d);
        }
        return dates;
    }


    private String createIndexByDates(LocalDateTime startDate, LocalDateTime endDate) {
        String start = startDate.getDayOfMonth() + "-" + startDate.getMonthValue() + "-" + startDate.getYear() + "-" + startDate.getHour() + startDate.getMinute() + startDate.getSecond();
        String end = endDate.getDayOfMonth() + "-" + endDate.getMonthValue() + "-" + endDate.getYear() + "-" + endDate.getHour() + endDate.getMinute() + endDate.getSecond();
        return start + "-" + end;
    }
}
