package com.test.digitalzone.reactiveregistration.repositories.implementations;

import com.test.digitalzone.reactiveregistration.repositories.interfaces.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private HyperLogLogOperations<String, String> operations;

    @PostConstruct
    public void init() {
        operations = redisTemplate.opsForHyperLogLog();
    }

    @Override
    public Long pfAdd(String key, String userId) {
        return operations.add(key, userId);
    }

    @Override
    public Long pfCount(String key) {
        return operations.size(key);
    }

    @Override
    public Long pfMerge(String key, List<String> keys) {
        String[] array = new String[keys.size()];
        keys.toArray(array);
        return operations.union(key, array);
    }


}
