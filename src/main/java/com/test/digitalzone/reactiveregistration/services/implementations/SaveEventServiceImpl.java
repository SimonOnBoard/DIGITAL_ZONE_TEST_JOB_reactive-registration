package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.dto.ViewEventDto;
import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.RedisService;
import com.test.digitalzone.reactiveregistration.services.interfaces.SaveEventService;
import com.test.digitalzone.reactiveregistration.services.interfaces.TablesService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class SaveEventServiceImpl implements SaveEventService {
    private final TablesService tablesService;
    private final RedisService redisService;
    private final ViewEventRepository viewEventRepository;

    @Async("threadPoolTaskExecutor")
    @Override
    public void saveEvent(ViewEventDto eventDto) {
        Future<String> nameToGet = tablesService.getCurrentTableName(eventDto.getUrl());
        try {
            String tableName = nameToGet.get();
            ListenableFuture<Long> listenableFuture = redisService.publishUserIdForToday(eventDto.getUserId());

            listenableFuture.addCallback(new ListenableFutureCallback<Long>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable.getMessage());
                }

                @Override
                public void onSuccess(Long aLong) {
                    if (aLong != 0) redisService.updateActualTodayNumber();
                }
            });

            viewEventRepository.save(
                    ViewEvent.builder().userId(eventDto.getUserId()).time(LocalDateTime.now()).build(), tableName);
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
