package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.services.interfaces.TimeConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@Service
@RequiredArgsConstructor
public class TimeConversionServiceImpl implements TimeConversionService {

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<LocalDateTime> getTimeForStart(Map<String, Integer> mapToConvert) throws ExecutionException, InterruptedException {
        checkMap(mapToConvert);
        LocalDate localDate;
        LocalDateTime result;
        if (mapToConvert.get("day") != null) {
            localDate = getLocalDateFromMap(mapToConvert);
        } else {
            localDate = LocalDate.of(mapToConvert.get("year"), mapToConvert.get("month"), 1);
        }
        return AsyncResult.forValue(LocalDateTime.of(localDate, getLocalTimeForMap(mapToConvert, true).get()));
    }

    private LocalDate getLocalDateFromMap(Map<String, Integer> mapToConvert) {
        return LocalDate.of(mapToConvert.get("year"), mapToConvert.get("month"), mapToConvert.get("day"));
    }

    private void checkMap(Map<String, Integer> mapToConvert) {
        if (mapToConvert.get("year") == null || mapToConvert.get("month") == null)
            throw new IllegalArgumentException("Can't find month or year in parameters od period");
    }


    @Async("threadPoolTaskExecutor")
    @Override
    public Future<LocalDateTime> getTimeForEnd(Map<String, Integer> mapToConvert) throws ExecutionException, InterruptedException {
        checkMap(mapToConvert);
        LocalDate localDate;
        if (mapToConvert.get("day") != null) {
            localDate = getLocalDateFromMap(mapToConvert);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, mapToConvert.get("month"));
            localDate = LocalDate.of(mapToConvert.get("year"), mapToConvert.get("month"), calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        return AsyncResult.forValue(LocalDateTime.of(localDate, getLocalTimeForMap(mapToConvert, false).get()));
    }

    @Override
    public Future<LocalTime> getLocalTimeForMap(Map<String, Integer> mapToConvert, boolean start) {
        LocalTime result;
        if (mapToConvert.get("hours") != null) {
            if (mapToConvert.get("minutes") != null) {
                if (mapToConvert.get("seconds") != null) {
                    if (mapToConvert.get("milliseconds") != null) {
                        result = LocalTime.of(mapToConvert.get("hours"), mapToConvert.get("minutes"), mapToConvert.get("seconds"), mapToConvert.get("milliseconds"));
                    } else {
                        result = LocalTime.of(mapToConvert.get("hours"), mapToConvert.get("minutes"), mapToConvert.get("seconds"));
                    }
                } else {
                    result = LocalTime.of(mapToConvert.get("hours"), mapToConvert.get("minutes"));
                }
            } else {
                if(start) {
                    result = LocalTime.of(mapToConvert.get("hours"), 0, 0);
                }else{
                    result = LocalTime.of(mapToConvert.get("hours"), 59, 59, 999999999);
                }
            }
        } else {
            if(start) {
                result = LocalTime.MIN;
            }else{
                result = LocalTime.MAX;
            }
        }
        return AsyncResult.forValue(result);
    }
}
