package com.test.digitalzone.reactiveregistration.services.implementations;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventFindQueuesRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsGenerationService;
import lombok.RequiredArgsConstructor;
import net.agkn.hll.HLL;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.swing.text.View;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.Adler32;

@Service
@RequiredArgsConstructor
public class StatisticsGenerationServiceImpl implements StatisticsGenerationService {
    private final ViewEventFindQueuesRepository viewEventFindQueuesRepository;
    private final RedisService redisService;

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> getCurrentAmountFromTableByStartAndTheEnd(LocalDateTime start, LocalDateTime end, String tableName) {
        if (!viewEventFindQueuesRepository.checkIfTableExists(tableName)) return AsyncResult.forValue(1L);
        Long startId = viewEventFindQueuesRepository.findIdByLowerBound(tableName, start);
        Long endId = viewEventFindQueuesRepository.findIdByUpperBound(tableName, end);
        if (endId == 0 | startId == 0) return AsyncResult.forValue(0L);
        return AsyncResult.forValue(endId - startId + 1);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<List<ViewEvent>> getAllEventsByTableBetween(LocalDateTime start, LocalDateTime end, String tableName) {
        return AsyncResult.forValue(viewEventFindQueuesRepository.findAllFromTableBetween(start, end, tableName));
    }


    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> addAllEventsToHLL(List<ViewEvent> events, HLL hll) {
        //не потокобезопасная штука, поэтому лучше каждый раз иметь новый экземпляр
        HashFunction hashFunction = Hashing.murmur3_128();
        if (events.size() == 0) return AsyncResult.forValue(0L);
        long resultx = 0;
        for (ViewEvent viewEvent : events) {
                resultx = hashFunction.newHasher().putString(viewEvent.getUserId(), Charset.defaultCharset()).hash().asLong();
                synchronized (hll) {
                    hll.addRaw(resultx);
                }
        }
        return AsyncResult.forValue(hll.cardinality());
    }

    @Async("threadPoolTaskExecutor")
    @Override
    //сознательно использую одну хеш функцию для равномерного размытия значений по блум фильтрам, здесь мне важно просто существование уникального пользователя
    public Future<byte[]> getDummyBloomFilter(int size, List<ViewEvent> viewEvents) {
        byte[] resultArray = new byte[size];
        //не потокобезопасная штука, поэтому лучше каждый раз иметь новый экземпляр
        HashFunction hashFunction = Hashing.murmur3_128();
        long result;
        for (ViewEvent viewEvent : viewEvents) {
            result = Math.abs(hashFunction.newHasher().putString(viewEvent.getUserId(), Charset.defaultCharset()).hash().asLong());
            resultArray[(int) (result % size)] += 1;
        }
        return AsyncResult.forValue(resultArray);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> calculateResultForRegularUsers(List<byte[]> resultBloomFilters) {
        long resultCounter = 0;
        int localCounter = 0;
        for (int i = 0; i < resultBloomFilters.get(0).length; i++) {
            localCounter = 0;
            for (byte[] hashArray : resultBloomFilters) {
                if (hashArray[i] != 0) localCounter++;
                if (localCounter >= 10) {
                    resultCounter++;
                    break;
                }
            }
        }
        return AsyncResult.forValue(resultCounter);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Long> calculateAllViews(List<List<ViewEvent>> listOfEvents) {
        long result = 0;
        for (List<ViewEvent> viewEvents : listOfEvents) {
            result += viewEvents.get(viewEvents.size() - 1).getId() - viewEvents.get(0).getId() + 1;
        }
        return AsyncResult.forValue(result);
    }

}
