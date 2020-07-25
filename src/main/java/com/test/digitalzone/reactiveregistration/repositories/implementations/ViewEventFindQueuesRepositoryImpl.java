package com.test.digitalzone.reactiveregistration.repositories.implementations;

import com.test.digitalzone.reactiveregistration.models.Table;
import com.test.digitalzone.reactiveregistration.models.ViewEvent;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventFindQueuesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ViewEventFindQueuesRepositoryImpl implements ViewEventFindQueuesRepository {
    private final JdbcTemplate jdbcTemplate;

    private final String SQL_FIND_BY_LOWER_BOUND_START = "SELECT id FROM " + "\"";
    private final String SQL_FIND_BY_LOWER_BOUND_END = "\"" + " where time >= ? order by id asc limit 1";
    private final String SQL_COUNT_BY_LOWER_BOUND_START = "SELECT count(id) FROM " + "\"";
    private final String SQL_COUNT_BY_LOWER_BOUND_END = "\"" + " where time >= ?";

    //language=sql
    private final String SQL_CHECK_IF_EXISTS = "SELECT EXISTS ( SELECT FROM pg_tables " +
            "WHERE  schemaname = 'public' AND tablename  = ?)";

    @Override
    public boolean checkIfTableExists(String tableName) {
        boolean result = jdbcTemplate.queryForObject(SQL_CHECK_IF_EXISTS, new Object[]{tableName},Boolean.class);
        return result;
    }

    @Override
    public Long findIdByLowerBound(String tableName, LocalDateTime time) {
        Long count = jdbcTemplate.queryForObject(
                SQL_COUNT_BY_LOWER_BOUND_START + tableName + SQL_COUNT_BY_LOWER_BOUND_END,
                new Object[]{time}, Long.class);
        return count != 0 ? jdbcTemplate.queryForObject(
                SQL_FIND_BY_LOWER_BOUND_START + tableName + SQL_FIND_BY_LOWER_BOUND_END,
                new Object[]{time}, Long.class) : count;
    }

    private final String SQL_FIND_BY_UPPER_BOUND_START = "SELECT id FROM " + "\"";
    private final String SQL_FIND_BY_UPPER_BOUND_END = "\"" + " where time <= ? order by id desc limit 1";
    private final String SQL_COUNT_BY_UPPER_BOUND_START = "SELECT count(id) FROM " + "\"";
    private final String SQL_COUNT_BY_UPPER_BOUND_END = "\"" + " where time <= ?";

    @Override
    public Long findIdByUpperBound(String tableName, LocalDateTime time) {
        Long count = jdbcTemplate.queryForObject(
                SQL_COUNT_BY_UPPER_BOUND_START + tableName + SQL_COUNT_BY_UPPER_BOUND_END,
                new Object[]{time}, Long.class);
        return count != 0 ? jdbcTemplate.queryForObject(
                SQL_FIND_BY_UPPER_BOUND_START + tableName + SQL_FIND_BY_UPPER_BOUND_END,
                new Object[]{time}, Long.class) : count;
    }


    private final String SQL_SELECT_ALL_BETWEEN_START = "SELECT * FROM " + "\"";
    private final String SQL_SELECT_ALL_BETWEEN_END = "\"" + " where time >= ? and time <= ? order by id asc";

    @Override
    public List<ViewEvent> findAllFromTableBetween(LocalDateTime start, LocalDateTime end, String table) {
        String sqlToRun = SQL_SELECT_ALL_BETWEEN_START + table + SQL_SELECT_ALL_BETWEEN_END;
        return jdbcTemplate.query(sqlToRun, new Object[]{start, end}, viewEventRowMapper);
    }

    private final RowMapper<ViewEvent> viewEventRowMapper = (row, rowNumber) ->
            ViewEvent.builder()
                    .id(row.getLong("id"))
                    .userId(row.getString("userId"))
                    .build();
}
