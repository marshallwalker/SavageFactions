package com.massivecraft.factions.zcore.persist.sql;

import com.massivecraft.factions.Conf;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SqlBuilder {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Map<ParameterizedPath, String> sqlCache = new HashMap<>();
    private final HikariDataSource dataSource;

    private ScheduledFuture task;
    private Connection batchConnection;

    private SqlBuilder() {
        Map<String, String> sqlConfig = Conf.sqlConfig;

        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(sqlConfig.get("jdbc"));
        dataSource.setUsername(sqlConfig.get("username"));
        dataSource.setPassword(sqlConfig.get("password"));

        this.task = executorService.scheduleAtFixedRate(this::commitBatch, 10, 10, TimeUnit.SECONDS);
    }

    private String getSqlQuery(ParameterizedPath path) {
        String query = sqlCache.get(path);

        if (query == null) {
            InputStream stream = getClass().getResourceAsStream("/sql/" + path.path + ".sql");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            query = String.format(reader.lines().collect(Collectors.joining(" ")), path.params);
            sqlCache.put(path, query);
        }

        return query;
    }

    private void setBatchConnection() {
        try {
            batchConnection = dataSource.getConnection();
            batchConnection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getBatchConnection() {
        try {
            if (batchConnection == null || batchConnection.isClosed()) {
                setBatchConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return batchConnection;
    }

    private void commitBatch() {
        try {
            if (batchConnection == null || batchConnection.isClosed()) {
                return;
            }

            batchConnection.commit();
            batchConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        task.cancel(false);
        commitBatch();
    }

    public SqlQuery path(String path, String... args) {
        String query = getSqlQuery(new ParameterizedPath(path, args));
        return new SqlQuery(executorService, this, dataSource, query);
    }

    private static SqlBuilder instance;

    public static SqlBuilder getInstance() {
        if (instance == null) {
            instance = new SqlBuilder();
        }

        return instance;
    }

    private static class ParameterizedPath {
        private String path;
        private String[] params;

        public ParameterizedPath(String path, String... params) {
            this.path = path;
            this.params = params;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ParameterizedPath)) return false;
            ParameterizedPath opath = (ParameterizedPath) other;
            return opath.path.equals(this.path) && Arrays.equals(this.params, opath.params);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new Object[]{
                    path,
                    params
            });
        }
    }
}