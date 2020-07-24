package com.test.digitalzone.reactiveregistration.repositories.interfaces;

import com.test.digitalzone.reactiveregistration.models.ViewEvent;

public interface ViewEventRepository {
    void save(ViewEvent event, String tableName);
}
