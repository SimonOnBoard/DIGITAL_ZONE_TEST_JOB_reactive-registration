package com.test.digitalzone.reactiveregistration.services.interfaces;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface TimeConversionService {
    Future<LocalDateTime> getTimeForStart(Map<String,Integer> mapToConvert) throws ExecutionException, InterruptedException;
    Future<LocalDateTime> getTimeForEnd(Map<String,Integer> mapToConvert) throws ExecutionException, InterruptedException;
    Future<LocalTime> getLocalTimeForMap(Map<String, Integer> mapToConvert, boolean b);
}
