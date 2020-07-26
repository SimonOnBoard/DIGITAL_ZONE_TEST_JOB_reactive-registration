package com.test.digitalzone.reactiveregistration.schedulers;

import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class ChangeDatesMidnightTasks {
    private final RedisService redisService;
    private final StatisticsService statisticsService;

    @Scheduled(cron = "0 0 * * * SUN-MON")
    public void reportCurrentTime() {
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2020, 7, 27), LocalTime.MIDNIGHT);
        statisticsService.setDayStart(localDateTime.toLocalDate().atTime(LocalTime.MIN));
        statisticsService.setDayEnd(localDateTime.toLocalDate().atTime(LocalTime.MAX));
        LocalDate date = localDateTime.toLocalDate();
        redisService.setDay(date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear());
        System.err.println("CHANGED");
    }
}