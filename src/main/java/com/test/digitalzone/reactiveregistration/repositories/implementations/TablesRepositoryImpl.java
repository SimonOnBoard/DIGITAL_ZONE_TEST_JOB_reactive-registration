package com.test.digitalzone.reactiveregistration.repositories.implementations;

import com.test.digitalzone.reactiveregistration.models.Table;
import com.test.digitalzone.reactiveregistration.repositories.interfaces.TablesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TablesRepositoryImpl implements TablesRepository {
    private final JdbcTemplate template;
    //language=sql
    private final String SQL_CREATE_TABLE_TABLES = "CREATE TABLE IF NOT EXISTS TABLES " +
            "(id bigserial primary key, name varchar(1000))";
    //language=sql
    private final String SQL_CREATE_INDEX_ON_TABLES = "CREATE INDEX CONCURRENTLY IF NOT EXISTS table_name_index on TABLES(name)";

    //language=sql
    private final String SQL_INSERT_INTO_TABLES = "INSERT INTO TABLES (name) values (?)";

    @Override
    public Table save(Table table) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_INTO_TABLES, new String[]{"id"});
            ps.setString(1, table.getName());
            return ps;
        }, keyHolder);
        List<Map<String, Object>> m = keyHolder.getKeyList();
        Map<String, Object> x = keyHolder.getKeys();
        table.setId((Long) keyHolder.getKey());
        return table;
    }

    private final RowMapper<Table> tableRowMapper = (row, rowNumber) ->
            Table.builder()
                    .id(row.getLong("id"))
                    .name(row.getString("name"))
                    .build();
    //language=sql
    private final String SQL_FIND_BY_NAME = "SELECT * FROM TABLES WHERE TABLES.name = ?";

    @Override
    public Table findByName(String name) {
        Long count = template.queryForObject("SELECT count(id) FROM TABLES WHERE TABLES.name = ?", new Object[]{name}, Long.class);
        if (count == 0) return null;
        if (count == 1) return template.queryForObject(SQL_FIND_BY_NAME, new Object[]{name}, tableRowMapper);
        throw new IllegalStateException("more then one row found in tables");
    }

    //language=sql
    private final String SQL_CREATE_TABLE_START = "CREATE TABLE IF NOT EXISTS";
    //language=sql
    private final String SQL_CREATE_TABLE_END = "(id bigserial primary key, userId varchar(1000), time timestamp);";

    @Override
    public void createTableById(String name) {
        template.execute(SQL_CREATE_TABLE_START + name + SQL_CREATE_TABLE_END);
    }

    @Override
    public void createTables() {
        template.execute(SQL_CREATE_TABLE_TABLES);
        template.execute(SQL_CREATE_INDEX_ON_TABLES);
    }

    //language=sql
    private final String SQL_FIND_ALL_NAMES = "SELECT id from TABLES";
    @Override
    public List<String> findAllNames() {
        return template.queryForList(SQL_FIND_ALL_NAMES, String.class);
    }
}
