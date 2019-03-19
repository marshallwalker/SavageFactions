package com.massivecraft.factions.zcore.persist.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class SqlQuery {
    private final ScheduledExecutorService executorService;
    private final SqlBuilder sqlBuilder;
    private final DataSource dataSource;
    private final String query;

    private Object[] args = {};

    public SqlQuery(ScheduledExecutorService executorService, SqlBuilder sqlBuilder, DataSource dataSource, String query) {
        this.executorService = executorService;
        this.sqlBuilder = sqlBuilder;
        this.dataSource = dataSource;
        this.query = query;
    }

    private void prepareStatement(Connection connection, boolean shouldCloseConnection, ThrowableConsumer<PreparedStatement, SQLException> consumer, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);

            for (int i = 1; i <= args.length; i++) {

                if(args[i- 1] instanceof UUID) {
                    statement.setObject(i, args[i - 1].toString());
                } else {
                    statement.setObject(i, args[i - 1]);
                }
            }

            consumer.accept(statement);
            statement.close();

            if (shouldCloseConnection) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println(">>> " + query);
            e.printStackTrace();
        }
    }

    private void prepareStatement(ThrowableConsumer<PreparedStatement, SQLException> consumer, Object... args) {
        try {
            prepareStatement(dataSource.getConnection(), true, consumer, args);
        } catch (SQLException e) {
            System.err.println(">>> " + query);
            e.printStackTrace();
        }
    }

    public SqlQuery args(Object... args) {
        this.args = args;
        return this;
    }

    public void execute() {
        prepareStatement(PreparedStatement::execute, args);
    }

    public void executeAsync() {
        executorService.submit(this::execute);
    }

    public void executeAsyncBatched() {
        executorService.submit(() -> prepareStatement(sqlBuilder.getBatchConnection(), false, PreparedStatement::execute, args));
    }

    public void query(ThrowableConsumer<ResultSet, SQLException> consumer) {
        prepareStatement(statement -> consumer.accept(statement.executeQuery()), args);
    }

    public void queryAsync(ThrowableConsumer<ResultSet, SQLException> consumer) {
        executorService.submit(() -> query(consumer));
    }

    public void queryEach(ThrowableConsumer<ResultSet, SQLException> consumer) {
        prepareStatement(statement -> {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                consumer.accept(resultSet);
            }

            resultSet.close();
        }, args);
    }

    public void queryEachAsync(ThrowableConsumer<ResultSet, SQLException> consumer) {
        executorService.submit(() -> queryEach(consumer));
    }
}