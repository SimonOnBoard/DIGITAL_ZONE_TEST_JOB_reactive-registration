package com.test.digitalzone.reactiveregistration.repositories.implementations;

import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

@Repository
@RequiredArgsConstructor
public class ViewEventRepositoryImpl implements ViewEventRepository {
    private final JdbcTemplate template;

    private final String SQL_INSERT_INTO_EVENTS_TABLE_START = "INSERT INTO";

    //language=sql
    private final String SQL_INSERT_INTO_EVENTS_TABLE_END = "(userId, time) VALUES (?,?)";


    @Override
    public void save(ViewEvent event, String tableName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_INTO_EVENTS_TABLE_START + tableName + SQL_INSERT_INTO_EVENTS_TABLE_END);
            ps.setString(1, event.getUserId());
            ps.setObject(2, event.getTime());
            return ps;
        }, keyHolder);
        event.setId((Long) keyHolder.getKey());
    }
}
