package com.company.directory.dao;

import com.company.directory.model.AppUser;
import com.company.directory.model.Role;
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
 * Data-access layer for the {@code app_user} table.
 *
 * <p>Every public method opens a fresh connection from the {@link Database}
 * pool, executes one SQL statement, and closes the connection automatically
 * via try-with-resources.  This keeps transactions simple (one statement = one
 * connection = auto-commit).
 *
 * <p>On any {@link java.sql.SQLException} an {@link com.company.directory.service.exception.AppException}
 * is thrown so callers never have to deal with checked SQL exceptions.
 */
public class AppUserDao {

    private static final String COLUMNS =
            "id, username, password_hash, role, active, must_change_password, "
            + "profile_completed, profile_locked, full_name, employee_id, department, "
            + "position, location, address, work_phone, mobile, email, created_at, updated_at";

    /** Returns the user with the given id, or {@code null} if not found. */
    public AppUser findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM app_user WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Could not load user " + id);
        }
    }

    /** Returns the user with the given username, or {@code null} if not found. */
    public AppUser findByUsername(String username) {
        String sql = "SELECT " + COLUMNS + " FROM app_user WHERE username = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Could not load user '" + username + "'");
        }
    }

    /** Returns {@code true} if any user (active or not) has this username. */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM app_user WHERE username = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AppException("Could not check username");
        }
    }

    /** Returns every user (active and inactive) sorted alphabetically by full name. */
    public List<AppUser> findAllOrderByFullName() {
        String sql = "SELECT " + COLUMNS + " FROM app_user ORDER BY full_name ASC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AppUser> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Could not list users");
        }
    }

    /** Inserts a new user and returns it with the generated id set. */
    public AppUser insert(AppUser user) {
        String sql = "INSERT INTO app_user "
                + "(username, password_hash, role, active, must_change_password, "
                + "profile_completed, profile_locked, full_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.setBoolean(5, user.isMustChangePassword());
            ps.setBoolean(6, user.isProfileCompleted());
            ps.setBoolean(7, user.isProfileLocked());
            ps.setString(8, user.getFullName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new AppException("Could not create the user");
        }
    }

    /** Updates every mutable column of the user. */
    public void update(AppUser u) {
        String sql = "UPDATE app_user SET password_hash=?, role=?, active=?, "
                + "must_change_password=?, profile_completed=?, profile_locked=?, "
                + "full_name=?, employee_id=?, department=?, position=?, location=?, "
                + "address=?, work_phone=?, mobile=?, email=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getPasswordHash());
            ps.setString(2, u.getRole().name());
            ps.setBoolean(3, u.isActive());
            ps.setBoolean(4, u.isMustChangePassword());
            ps.setBoolean(5, u.isProfileCompleted());
            ps.setBoolean(6, u.isProfileLocked());
            ps.setString(7, u.getFullName());
            ps.setString(8, u.getEmployeeId());
            ps.setString(9, u.getDepartment());
            ps.setString(10, u.getPosition());
            ps.setString(11, u.getLocation());
            ps.setString(12, u.getAddress());
            ps.setString(13, u.getWorkPhone());
            ps.setString(14, u.getMobile());
            ps.setString(15, u.getEmail());
            ps.setLong(16, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Could not update the user");
        }
    }

    /** Counts active users matching the search term across name/department/phone. */
    public long countActiveMatching(String term) {
        String sql = "SELECT COUNT(*) FROM app_user WHERE active = TRUE AND ("
                + "? = '' OR LOWER(full_name) LIKE ? OR LOWER(department) LIKE ? "
                + "OR LOWER(work_phone) LIKE ? OR LOWER(mobile) LIKE ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + term.toLowerCase() + "%";
            ps.setString(1, term);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new AppException("Could not count directory results");
        }
    }

    /** One page of active users matching the term, ordered by name. */
    public List<AppUser> searchActive(String term, int offset, int limit) {
        String sql = "SELECT " + COLUMNS + " FROM app_user WHERE active = TRUE AND ("
                + "? = '' OR LOWER(full_name) LIKE ? OR LOWER(department) LIKE ? "
                + "OR LOWER(work_phone) LIKE ? OR LOWER(mobile) LIKE ?) "
                + "ORDER BY full_name ASC LIMIT ? OFFSET ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + term.toLowerCase() + "%";
            ps.setString(1, term);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setInt(6, limit);
            ps.setInt(7, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<AppUser> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new AppException("Could not search the directory");
        }
    }

    private AppUser mapRow(ResultSet rs) throws SQLException {
        AppUser u = new AppUser();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        u.setMustChangePassword(rs.getBoolean("must_change_password"));
        u.setProfileCompleted(rs.getBoolean("profile_completed"));
        u.setProfileLocked(rs.getBoolean("profile_locked"));
        u.setFullName(rs.getString("full_name"));
        u.setEmployeeId(rs.getString("employee_id"));
        u.setDepartment(rs.getString("department"));
        u.setPosition(rs.getString("position"));
        u.setLocation(rs.getString("location"));
        u.setAddress(rs.getString("address"));
        u.setWorkPhone(rs.getString("work_phone"));
        u.setMobile(rs.getString("mobile"));
        u.setEmail(rs.getString("email"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());
        if (updated != null) u.setUpdatedAt(updated.toLocalDateTime());
        return u;
    }
}
