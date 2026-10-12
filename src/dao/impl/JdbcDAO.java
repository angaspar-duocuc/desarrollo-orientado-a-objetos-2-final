package dao.impl;

import util.AppException;
import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

abstract class JdbcDAO<T> {
    protected final DatabaseConnection db = DatabaseConnection.getInstance();
    @FunctionalInterface protected interface Mapper<T> { T map(ResultSet row) throws SQLException; }
    protected List<T> query(String sql, Mapper<T> mapper, Object... values) {
        return db.execute(c -> {
            try (PreparedStatement statement = c.prepareStatement(sql)) {
                bind(statement, values);
                try (ResultSet rows = statement.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (rows.next()) result.add(mapper.map(rows));
                    return result;
                }
            }
        });
    }
    protected int insert(String sql, Object... values) {
        return db.execute(c -> {
            try (PreparedStatement statement = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, values);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Missing generated key");
                    return keys.getInt(1);
                }
            }
        });
    }
    protected void change(String sql, Object... values) {
        db.execute(c -> {
            try (PreparedStatement statement = c.prepareStatement(sql)) {
                bind(statement, values);
                if (statement.executeUpdate() != 1)
                    throw new AppException("El registro cambió o ya no existe. Recargue antes de continuar.");
                return null;
            }
        });
    }
    protected static void bind(PreparedStatement statement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof java.time.LocalDate date) statement.setDate(i + 1, java.sql.Date.valueOf(date));
            else statement.setObject(i + 1, value);
        }
    }
}