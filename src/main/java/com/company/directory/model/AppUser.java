package com.company.directory.model;

import java.time.LocalDateTime;

/**
 * Represents a single user account in the system.
 *
 * <p>This class combines two concerns that belong together:
 * <ul>
 *   <li><b>Auth fields</b> ({@code username}, {@code passwordHash}, {@code role},
 *       {@code active}, {@code mustChangePassword}) — used by {@code AuthFilter}
 *       and the login flow.</li>
 *   <li><b>Directory profile fields</b> ({@code fullName}, {@code department},
 *       phones, email, …) — shown in the employee directory.</li>
 * </ul>
 *
 * <p>The {@code profileCompleted} flag is {@code true} once the user has saved
 * their profile at least once.  {@code profileLocked} is set to {@code true}
 * at that same moment and prevents further self-edits — the user must raise an
 * {@link EditRequest} to unlock.  A Super Admin is never subject to the lock.
 */
public class AppUser {

    private Long id;
    private String username;
    private String passwordHash;
    private Role role;
    private boolean active = true;
    private boolean mustChangePassword = true;
    private boolean profileCompleted = false;
    private boolean profileLocked = false;
    private String fullName;
    private String employeeId;
    private String department;
    private String position;
    private String location;
    private String address;
    private String workPhone;
    private String mobile;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean v) { this.mustChangePassword = v; }
    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean v) { this.profileCompleted = v; }
    public boolean isProfileLocked() { return profileLocked; }
    public void setProfileLocked(boolean v) { this.profileLocked = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getWorkPhone() { return workPhone; }
    public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
