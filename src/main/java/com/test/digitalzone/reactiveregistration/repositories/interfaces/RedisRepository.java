package com.test.digitalzone.reactiveregistration.repositories.interfaces;

public interface RedisRepository {
    public Long pfAdd(String key, String userId);
    public Long pfCount(String key);
}
