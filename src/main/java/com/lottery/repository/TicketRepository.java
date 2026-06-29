package com.lottery.repository;

import com.lottery.model.Ticket;
import com.lottery.model.TicketStatus;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TicketRepository {

    private final DataSource dataSource;

    public TicketRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ticket save(Ticket ticket) {
        String sql = """
                INSERT INTO tickets (draw_id, numbers, status, created_at)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ticket.getDrawId());
            setIntegerArray(statement, 2, ticket.getNumbers(), connection);
            statement.setString(3, ticket.getStatus().name());
            statement.setTimestamp(4, Timestamp.from(ticket.getCreatedAt()));

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                ticket.setId(rs.getLong("id"));
            }
            return ticket;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save ticket", e);
        }
    }

    public Optional<Ticket> findById(Long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
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
            throw new IllegalStateException("Failed to find ticket", e);
        }
    }

    public List<Ticket> findByDrawId(Long drawId) {
        String sql = "SELECT * FROM tickets WHERE draw_id = ? ORDER BY created_at";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, drawId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
                return tickets;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find tickets by draw", e);
        }
    }

    public void updateStatus(Long ticketId, TicketStatus status) {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, ticketId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update ticket status", e);
        }
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setDrawId(rs.getLong("draw_id"));
        ticket.setNumbers(readIntegerArray(rs.getArray("numbers")));
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        ticket.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return ticket;
    }

    private void setIntegerArray(PreparedStatement statement, int index, List<Integer> values, Connection connection)
            throws SQLException {
        Array array = connection.createArrayOf("integer", values.toArray());
        statement.setArray(index, array);
    }

    private List<Integer> readIntegerArray(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        Integer[] values = (Integer[]) array.getArray();
        return values == null ? List.of() : Arrays.asList(values);
    }
}
