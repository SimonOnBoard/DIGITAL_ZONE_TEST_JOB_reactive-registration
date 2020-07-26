package com.test.digitalzone.reactiveregistration.services.interfaces;

import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import net.agkn.hll.HLL;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.concurrent.Future;
import java.util.*;

public interface StatisticsGenerationService {
    Future<Long> getCurrentAmountFromTableByStartAndTheEnd(LocalDateTime start, LocalDateTime end, String tableName);
    Future<List<ViewEvent>> getAllEventsByTableBetween(LocalDateTime start, LocalDateTime end, String tableName);
    Future<Long> addAllEventsToHLL(List<ViewEvent> events, HLL hll);
    Future<byte[]> getDummyBloomFilter(int size, List<ViewEvent> viewEvents);
    Future<Long> calculateResultForRegularUsers(List<byte[]> resultBloomFilters);
    Future<Long> calculateAllViews(List<List<ViewEvent>> listOfEvents);
}
