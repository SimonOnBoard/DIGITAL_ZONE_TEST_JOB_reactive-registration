package com.test.digitalzone.reactiveregistration.controllers;


import com.test.digitalzone.reactiveregistration.dto.StatisticsDto;
import com.test.digitalzone.reactiveregistration.dto.ViewEventDto;
import com.test.digitalzone.reactiveregistration.services.interfaces.SaveEventService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsService;
import com.test.digitalzone.reactiveregistration.services.interfaces.TimeConversionService;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final SaveEventService saveEventService;
    private final StatisticsService statisticsService;
    private final TimeConversionService timeConversionService;

    @PostMapping("/registerEvent")
    public StatisticsDto addViewEvent(@RequestBody ViewEventDto eventDto) {
        saveEventService.saveEvent(eventDto);
        return statisticsService.getCurrentDayStatistics();
    }

    @GetMapping("/getPeriodStatistics")
    public StatisticsDto getStatisticsForPeriod(@RequestBody List<Map<String, Integer>> mapList) {
        Future<LocalDateTime> startDateCandidate = timeConversionService.getTimeForStart(mapList.get(0));
        Future<LocalDateTime> endDateCandidate = timeConversionService.getTimeForEnd(mapList.get(1));
        try {
            return statisticsService.getStatisticsByDates(startDateCandidate.get(), endDateCandidate.get()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
