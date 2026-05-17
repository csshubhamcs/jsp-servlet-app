package com.company.directory.dao;

import com.company.directory.model.EditRequest;
import com.company.directory.model.RequestStatus;
import com.company.directory.service.exception.AppException;
import com.company.directory.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the {@code edit_request} table.
 *
 * <p>Queries JOIN to {@code app_user} to fetch the requester's display name
 * ({@code requester_name}) in a single round-trip rather than a separate lookup.
 *
 * <p>Like {@link AppUserDao}, each method borrows one connection from
 * {@link com.company.directory.util.Database} and closes it automatically.
 */
public class EditRequestDao {

    /** Returns the request with the given id (with requester name joined), or {@code null}. */
    public EditRequest findById(long id) {
        String sql = "SELECT r.id, r.requester_id, u.full_name AS requester_name, "
                + "r.reason, r.status, r.created_at, r.resolved_by_id, r.resolved_at "
                + "FROM edit_request r JOIN app_user u ON u.id = r.requester_id "
                + "WHERE r.id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Could not load the edit request");
        }
    }

    /**
     * Returns all requests with the given status, oldest first.
     * The admin queue always shows {@code PENDING} requests in submission order.
     */
    public List<EditRequest> findByStatusOldestFirst(RequestStatus status) {
        String sql = "SELECT r.id, r.requester_id, u.full_name AS requester_name, "
                + "r.reason, r.status, r.created_at, r.resolved_by_id, r.resolved_at "
                + "FROM edit_request r JOIN app_user u ON u.id = r.requester_id "
                + "WHERE r.status = ? ORDER BY r.created_at ASC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<EditRequest> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new AppException("Could not list edit requests");
        }
    }

    /** Returns {@code true} if the user already has an unresolved (PENDING) request. */
    public boolean existsPendingForRequester(long requesterId) {
        String sql = "SELECT 1 FROM edit_request WHERE requester_id = ? AND status = 'PENDING'";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AppException("Could not check pending requests");
        }
    }

    /** Persists a new request and sets the generated {@code id} on the returned object. */
    public EditRequest insert(EditRequest r) {
        String sql = "INSERT INTO edit_request (requester_id, reason, status) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getRequesterId());
            ps.setString(2, r.getReason());
            ps.setString(3, r.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }
            return r;
        } catch (SQLException e) {
            throw new AppException("Could not save the edit request");
        }
    }

    /** Marks a request resolved (APPROVED or REJECTED) by the given admin, now. */
    public void resolve(long requestId, RequestStatus status, long resolvedById) {
        String sql = "UPDATE edit_request SET status = ?, resolved_by_id = ?, "
                + "resolved_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, resolvedById);
            ps.setLong(3, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Could not update the edit request");
        }
    }

    private EditRequest mapRow(ResultSet rs) throws SQLException {
        EditRequest r = new EditRequest();
        r.setId(rs.getLong("id"));
        r.setRequesterId(rs.getLong("requester_id"));
        r.setRequesterName(rs.getString("requester_name"));
        r.setReason(rs.getString("reason"));
        r.setStatus(RequestStatus.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        long resolvedBy = rs.getLong("resolved_by_id");
        if (!rs.wasNull()) r.setResolvedById(resolvedBy);
        Timestamp resolved = rs.getTimestamp("resolved_at");
        if (resolved != null) r.setResolvedAt(resolved.toLocalDateTime());
        return r;
    }
}
