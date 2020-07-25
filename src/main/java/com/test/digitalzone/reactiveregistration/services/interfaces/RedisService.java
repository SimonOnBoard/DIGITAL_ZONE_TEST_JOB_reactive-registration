package com.test.digitalzone.reactiveregistration.services.interfaces;

import org.springframework.util.concurrent.ListenableFuture;
import java.util.*;
public interface RedisService {
    ListenableFuture<Long> publishUserIdForToday(String userId);
    void updateActualTodayNumber();
    Long getTodayUniqueNumber();


    Long publishUserId(String userId, String key);
    Long getUniqueNumberForTag(String key);

    Long mergeIndexes(String key, List<String> keys);
}
