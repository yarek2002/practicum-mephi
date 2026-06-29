package com.lottery.repository;

import com.lottery.model.Draw;
import com.lottery.model.DrawStatus;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DrawRepository {

    private final DataSource dataSource;

    public DrawRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Draw save(Draw draw) {
        String sql = """
                INSERT INTO draws (status, numbers_count, max_number, winning_numbers, created_at, completed_at)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, draw.getStatus().name());
            statement.setInt(2, draw.getNumbersCount());
            statement.setInt(3, draw.getMaxNumber());
            setIntegerArray(statement, 4, draw.getWinningNumbers());
            statement.setTimestamp(5, Timestamp.from(draw.getCreatedAt()));
            if (draw.getCompletedAt() != null) {
                statement.setTimestamp(6, Timestamp.from(draw.getCompletedAt()));
            } else {
                statement.setNull(6, Types.TIMESTAMP_WITH_TIMEZONE);
            }

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                draw.setId(rs.getLong("id"));
            }
            return draw;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save draw", e);
        }
    }

    public Optional<Draw> findById(Long id) {
        String sql = "SELECT * FROM draws WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find draw", e);
        }
    }

    public List<Draw> findByStatus(DrawStatus status) {
        String sql = "SELECT * FROM draws WHERE status = ? ORDER BY created_at DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet rs = statement.executeQuery()) {
                List<Draw> draws = new ArrayList<>();
                while (rs.next()) {
                    draws.add(mapRow(rs));
                }
                return draws;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find draws by status", e);
        }
    }

    public Draw update(Draw draw) {
        String sql = """
                UPDATE draws
                SET status = ?, winning_numbers = ?, completed_at = ?
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, draw.getStatus().name());
            setIntegerArray(statement, 2, draw.getWinningNumbers());
            if (draw.getCompletedAt() != null) {
                statement.setTimestamp(3, Timestamp.from(draw.getCompletedAt()));
            } else {
                statement.setNull(3, Types.TIMESTAMP_WITH_TIMEZONE);
            }
            statement.setLong(4, draw.getId());
            statement.executeUpdate();
            return draw;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update draw", e);
        }
    }

    private Draw mapRow(ResultSet rs) throws SQLException {
        Draw draw = new Draw();
        draw.setId(rs.getLong("id"));
        draw.setStatus(DrawStatus.valueOf(rs.getString("status")));
        draw.setNumbersCount(rs.getInt("numbers_count"));
        draw.setMaxNumber(rs.getInt("max_number"));
        draw.setWinningNumbers(readIntegerArray(rs.getArray("winning_numbers")));
        draw.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        draw.setCompletedAt(completedAt != null ? completedAt.toInstant() : null);
        return draw;
    }

    private void setIntegerArray(PreparedStatement statement, int index, List<Integer> values) throws SQLException {
        if (values == null || values.isEmpty()) {
            statement.setNull(index, Types.ARRAY);
            return;
        }
        Connection connection = statement.getConnection();
        Array array = connection.createArrayOf("integer", values.toArray());
        statement.setArray(index, array);
    }

    private List<Integer> readIntegerArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Integer[] values = (Integer[]) array.getArray();
        return values == null ? null : Arrays.asList(values);
    }
}
