package com.test.digitalzone.reactiveregistration.services.interfaces;

import java.util.List;
import java.util.concurrent.Future;

public interface TablesService {
    Future<String> getCurrentTableName(String url);
    void createTableById(Long id);
    List<String> getAllTables();
}
