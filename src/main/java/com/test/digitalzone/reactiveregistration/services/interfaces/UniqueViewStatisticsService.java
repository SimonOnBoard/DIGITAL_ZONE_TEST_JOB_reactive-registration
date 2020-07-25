package com.test.digitalzone.reactiveregistration.services.interfaces;

import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

public interface UniqueViewStatisticsService {
    Future<Pair<Boolean,String>> getNameOfMergedUniqueIndex(LocalDateTime start, LocalDateTime end);
}
