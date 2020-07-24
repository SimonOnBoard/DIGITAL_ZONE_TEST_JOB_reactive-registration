package com.test.digitalzone.reactiveregistration.repositories.interfaces;

import java.time.LocalDateTime;

public interface ViewEventFindQueuesRepository {
    Long findIdByLowerBound(String tableName, LocalDateTime bound);
    Long findIdByUpperBound(String tableName, LocalDateTime bound);
}
