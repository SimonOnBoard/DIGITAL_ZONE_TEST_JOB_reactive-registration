package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.repositories.interfaces.RedisRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private Long actualCount;
    private final RedisRepository redisRepository;
    private String day;

    @PostConstruct
    public void init() {
        LocalDate date = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDate();
        day = date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear();
        System.err.println(day);
        actualCount = redisRepository.pfCount(day);
        System.err.println(actualCount);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public ListenableFuture<Long> publishUserIdForToday(String userId) {
        return AsyncResult.forValue(redisRepository.pfAdd(day.toString(), userId));
    }

    @Override
    public void updateActualTodayNumber() {
        actualCount = redisRepository.pfCount(day.toString());
    }

    @Override
    public Long getTodayUniqueNumber() {
        return actualCount;
    }

    @Override
    public Long publishUserId(String userId, String key) {
        return redisRepository.pfAdd(key, userId);
    }

    @Override
    public Long getUniqueNumberForTag(String key) {
        return redisRepository.pfCount(key);
    }


}
