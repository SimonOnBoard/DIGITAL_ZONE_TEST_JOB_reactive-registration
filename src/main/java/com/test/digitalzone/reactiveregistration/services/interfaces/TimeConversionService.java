package com.test.digitalzone.reactiveregistration.services.interfaces;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

public interface TimeConversionService {
    Future<LocalDateTime> getTimeForStart(Map<String,Integer> mapToConvert);
    Future<LocalDateTime> getTimeForEnd(Map<String,Integer> mapToConvert);
}
