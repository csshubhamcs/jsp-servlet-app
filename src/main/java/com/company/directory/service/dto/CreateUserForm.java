package com.company.directory.service.dto;

import com.company.directory.model.Role;

/**
 * Form data submitted when a Super Admin creates a new user account.
 *
 * <p>The servlet reads HTTP parameters into this object, then passes it to
 * {@link com.company.directory.service.UserService#createUser(CreateUserForm)},
 * which validates, hashes the password, and persists the new account.
 */
public class CreateUserForm {
    private String fullName;
    private String username;
    private String password;
    private Role role;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
