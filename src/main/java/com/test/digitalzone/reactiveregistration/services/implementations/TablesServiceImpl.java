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
    public void init(){
        tablesRepository.createTables();
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<String> getCurrentTableName(String url) {
        Table table = tablesRepository.findByName(url);
        if(table == null){
            table = tablesRepository.save(Table.builder().name(url).build());
            createTableById(table.getId());
        }
        return AsyncResult.forValue(getStringFromId(table.getId()));
    }
    @Override
    public void createTableById(Long id) {
        try {
          tablesRepository.createTableById(getStringFromId(id));
        } catch (RuntimeException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public List<String> getAllTables() {
        return tablesRepository.findAllNames();
    }

    public String getStringFromId(Long id){
        return "\"" + id.toString() + "\"";
    }
}
