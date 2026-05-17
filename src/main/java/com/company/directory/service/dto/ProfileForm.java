package com.company.directory.service.dto;

/**
 * Form data for updating a user's directory profile.
 *
 * <p>All fields are optional except {@code fullName}.  The servlet reads HTTP
 * parameters into this object and hands it to either
 * {@link com.company.directory.service.UserService#updateOwnProfile} (normal
 * users, one-time edit then locked) or
 * {@link com.company.directory.service.UserService#updateProfileAsSuperAdmin}
 * (Super Admins, always editable).
 */
public class ProfileForm {
    private String fullName;
    private String employeeId;
    private String department;
    private String position;
    private String location;
    private String address;
    private String workPhone;
    private String mobile;
    private String email;

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
}
