package com.test.digitalzone.reactiveregistration.services.interfaces;

import java.util.List;
import java.util.concurrent.Future;

public interface TablesService {
    String getCurrentTableName(String url);
    void createTableById(Long id);
    Future<List<String>> getAllTables();
}
