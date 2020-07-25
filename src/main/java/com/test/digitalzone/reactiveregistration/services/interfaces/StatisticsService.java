package com.test.digitalzone.reactiveregistration.services.interfaces;

import com.test.digitalzone.reactiveregistration.dto.StatisticsDto;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

public interface StatisticsService {
    StatisticsDto getCurrentDayStatistics();
    Future<StatisticsDto> getStatisticsByDates(LocalDateTime start, LocalDateTime end);
}
