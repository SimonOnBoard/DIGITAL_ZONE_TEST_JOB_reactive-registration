package com.test.digitalzone.reactiveregistration.services.interfaces;

import org.springframework.util.concurrent.ListenableFuture;

public interface RedisService {
    ListenableFuture<Long> publishUserIdForToday(String userId);
    void updateActualTodayNumber();
    Long getTodayUniqueNumber();


    Long publishUserId(String userId, String key);
    Long getUniqueNumberForTag(String key);
}
