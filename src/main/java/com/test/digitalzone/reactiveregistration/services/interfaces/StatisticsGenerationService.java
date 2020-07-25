package com.test.digitalzone.reactiveregistration.services.interfaces;

import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.concurrent.Future;
import java.util.*;

public interface StatisticsGenerationService {
    Future<Long> getCurrentAmountFromTableByStartAndTheEnd(LocalDateTime start, LocalDateTime end, String tableName);
    Future<List<ViewEvent>> getAllEventsByTableBetween(LocalDateTime start, LocalDateTime end, String tableName);
    Future<Boolean> addAllEventsToIndex(Pair<Boolean, String> caseAndIndexName, List<ViewEvent> events, LocalDateTime start, LocalDateTime end, String s);
    Future<byte[]> getResultDummyBloomFilter(int size, List<ViewEvent> viewEvents);
    Future<Long> calculateResultForRegularUsers(List<byte[]> resultBloomFilters);
    Future<Long> calculateAllViews(List<List<ViewEvent>> listOfEvents);
}
