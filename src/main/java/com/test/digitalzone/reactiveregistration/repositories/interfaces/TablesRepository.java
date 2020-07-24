package com.test.digitalzone.reactiveregistration.repositories.interfaces;

import com.test.digitalzone.reactiveregistration.models.Table;

import java.util.List;

public interface TablesRepository {
    Table save(Table table);
    Table findByName(String name);
    void createTableById(String toString);
    void createTables();

    List<String> findAllNames();
}
