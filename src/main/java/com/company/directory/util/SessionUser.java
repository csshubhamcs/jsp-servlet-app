package com.company.directory.util;

import com.company.directory.model.AppUser;
import com.company.directory.model.Role;

/**
 * A lightweight, serialisable snapshot of the logged-in user, stored in the
 * HTTP session under the key {@link #SESSION_KEY}.
 *
 * <p>Only the fields needed by the filter and views are stored here — no
 * password hash, no profile data.  This keeps the session small and avoids
 * stale data issues for fields that are rarely changed.
 *
 * <p>{@code mustChangePassword} is the one exception to immutability: it is
 * cleared in memory immediately after a successful password change so the
 * {@link com.company.directory.filter.AuthFilter} stops redirecting within
 * the same session without requiring a logout-and-back-in cycle.
 */
public class SessionUser {

    public static final String SESSION_KEY = "sessionUser";

    private final long id;
    private final String username;
    private final String fullName;
    private final Role role;
    private boolean mustChangePassword;

    public SessionUser(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.mustChangePassword = user.isMustChangePassword();
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean v) { this.mustChangePassword = v; }

    /** Returns {@code true} for ADMIN and SUPER_ADMIN roles. */
    public boolean isAdminOrAbove() {
        return role == Role.ADMIN || role == Role.SUPER_ADMIN;
    }

    /** Returns {@code true} only for SUPER_ADMIN. */
    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }
}
