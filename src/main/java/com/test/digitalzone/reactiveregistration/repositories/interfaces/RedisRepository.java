package com.test.digitalzone.reactiveregistration.repositories.interfaces;

import java.util.List;

public interface RedisRepository {
    public Long pfAdd(String key, String userId);

    public Long pfCount(String key);

    public Long pfMerge(String key, List<String> keys);
}
