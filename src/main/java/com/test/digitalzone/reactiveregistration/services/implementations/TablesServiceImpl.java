package com.test.digitalzone.reactiveregistration.services.implementations;

import com.test.digitalzone.reactiveregistration.models.Table;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.TablesRepository;
import com.test.digitalzone.reactiveregistration.services.interfaces.TablesService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class TablesServiceImpl implements TablesService {
    private final TablesRepository tablesRepository;

    @PostConstruct
    public void init() {
        tablesRepository.createTables();
    }

    @Override
    public synchronized String getCurrentTableName(String url) {
        Table table = tablesRepository.findByName(url);
        if (table == null) {
            table = tablesRepository.save(Table.builder().name(url).build());
            createTableById(table.getId());
        }
        return getStringFromId(table.getId());
    }

    @Override
    public void createTableById(Long id) {
        tablesRepository.createTableById(getStringFromId(id));
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<List<String>> getAllTables() {
        return AsyncResult.forValue(tablesRepository.findAllNames());
    }

    public String getStringFromId(Long id) {
        return "\"" + id.toString() + "\"";
    }
}
