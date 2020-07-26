package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.repositories.interfaces.RedisRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private Long actualCount;
    private final RedisRepository redisRepository;

    @Setter
    private String day;

    @PostConstruct
    public void init() {
        LocalDate date = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDate();
        day = date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear();
        actualCount = redisRepository.pfCount(day);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public ListenableFuture<Long> publishUserIdForToday(String userId) {
        return AsyncResult.forValue(redisRepository.pfAdd(day, userId));
    }

    @Override
    public void updateActualTodayNumber() {
        actualCount = redisRepository.pfCount(day);
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

    @Override
    public Long mergeIndexes(String key, List<String> keys) {
        return redisRepository.pfMerge(key, keys);
    }


}
