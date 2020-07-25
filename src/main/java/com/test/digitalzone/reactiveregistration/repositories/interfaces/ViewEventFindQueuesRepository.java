package com.test.digitalzone.reactiveregistration.repositories.interfaces;

import com.test.digitalzone.reactiveregistration.models.Table;
import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
public interface ViewEventFindQueuesRepository {
    boolean checkIfTableExists(String tableName);
    Long findIdByLowerBound(String tableName, LocalDateTime bound);
    Long findIdByUpperBound(String tableName, LocalDateTime bound);

    List<ViewEvent> findAllFromTableBetween(LocalDateTime start, LocalDateTime end, String table);
}
