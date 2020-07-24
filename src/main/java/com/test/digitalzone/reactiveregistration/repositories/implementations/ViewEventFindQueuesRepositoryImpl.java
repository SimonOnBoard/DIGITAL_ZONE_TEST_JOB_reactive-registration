package com.test.digitalzone.reactiveregistration.repositories.implementations;

import com.test.digitalzone.reactiveregistration.repositories.interfaces.ViewEventFindQueuesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ViewEventFindQueuesRepositoryImpl implements ViewEventFindQueuesRepository {
    private final JdbcTemplate jdbcTemplate;

    private final String SQL_FIND_BY_LOWER_BOUND_START = "SELECT id FROM " + "\"";
    private final String SQL_FIND_BY_LOWER_BOUND_END = "\"" + " where time >= ? order by id asc limit 1";
    private final String SQL_COUNT_BY_LOWER_BOUND_START = "SELECT count(id) FROM " + "\"";
    private final String SQL_COUNT_BY_LOWER_BOUND_END = "\"" + " where time >= ?";

    @Override
    public Long findIdByLowerBound(String tableName, LocalDateTime time) {
        String resultCountRequest = SQL_COUNT_BY_LOWER_BOUND_START + tableName
                + SQL_COUNT_BY_LOWER_BOUND_END;
        String resultFindRequest = SQL_FIND_BY_LOWER_BOUND_START + tableName
                + SQL_FIND_BY_LOWER_BOUND_END;
        Long count = jdbcTemplate.queryForObject(resultCountRequest, new Object[]{time}, Long.class);
        return count != 0 ? jdbcTemplate.queryForObject(resultFindRequest, new Object[]{time}, Long.class) : count;
    }

    private final String SQL_FIND_BY_UPPER_BOUND_START = "SELECT id FROM " + "\"";
    private final String SQL_FIND_BY_UPPER_BOUND_END = "\"" + " where time <= ? order by id desc limit 1";
    private final String SQL_COUNT_BY_UPPER_BOUND_START = "SELECT count(id) FROM " + "\"";
    private final String SQL_COUNT_BY_UPPER_BOUND_END = "\"" + " where time <= ?";

    @Override
    public Long findIdByUpperBound(String tableName, LocalDateTime time) {
        Long count = jdbcTemplate.queryForObject(SQL_COUNT_BY_UPPER_BOUND_START + tableName
                + SQL_COUNT_BY_UPPER_BOUND_END, new Object[]{time}, Long.class);
        return count != 0 ? jdbcTemplate.queryForObject(SQL_FIND_BY_UPPER_BOUND_START + tableName
                + SQL_FIND_BY_UPPER_BOUND_END, new Object[]{time}, Long.class) : count;
    }
}
