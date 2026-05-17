package com.company.directory.service;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import com.company.directory.model.Role;
import com.company.directory.service.dto.CreateUserForm;
import com.company.directory.service.dto.ProfileForm;
import com.company.directory.service.exception.AppException;

/**
 * Business logic for user accounts, profiles, and passwords.
 *
 * <p>This is the single entry point for all mutations to {@code AppUser} data.
 * It enforces business rules (e.g. one-time profile edit, password hashing,
 * username uniqueness) before delegating persistence to {@link AppUserDao}.
 *
 * <p>All error cases throw {@link com.company.directory.service.exception.AppException}
 * with a message that is safe to display directly to the user.
 */
public class UserService {

    private final AppUserDao userDao;

    public UserService(AppUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Loads a user by id, throwing if they do not exist.
     * Used by all other methods as a safe load.
     */
    public AppUser getById(long id) {
        AppUser user = userDao.findById(id);
        if (user == null) {
            throw new AppException("User not found.");
        }
        return user;
    }

    /**
     * Creates a new account from the supplied form.
     * The password is hashed with BCrypt; {@code mustChangePassword} is set so
     * the user is forced to choose their own password on first login.
     *
     * @throws com.company.directory.service.exception.AppException if the username is already taken
     */
    public AppUser createUser(CreateUserForm form) {
        if (userDao.existsByUsername(form.getUsername())) {
            throw new AppException("That username is already taken.");
        }
        AppUser user = new AppUser();
        user.setUsername(form.getUsername());
        user.setFullName(form.getFullName());
        user.setRole(form.getRole());
        user.setPasswordHash(PasswordUtil.hash(form.getPassword()));
        user.setActive(true);
        user.setMustChangePassword(true);
        user.setProfileCompleted(false);
        user.setProfileLocked(false);
        return userDao.insert(user);
    }

    /** A normal user editing their own profile — allowed once, then it locks. */
    public void updateOwnProfile(long userId, ProfileForm form) {
        AppUser user = getById(userId);
        if (user.isProfileLocked()) {
            throw new AppException("Your profile is locked. Request an edit to make changes.");
        }
        applyProfile(user, form);
        user.setProfileCompleted(true);
        user.setProfileLocked(true);
        userDao.update(user);
    }

    /** A Super Admin editing any profile — no lock check, never locks. */
    public void updateProfileAsSuperAdmin(long userId, ProfileForm form) {
        AppUser user = getById(userId);
        applyProfile(user, form);
        user.setProfileCompleted(true);
        userDao.update(user);
    }

    private void applyProfile(AppUser user, ProfileForm form) {
        user.setFullName(form.getFullName());
        user.setEmployeeId(form.getEmployeeId());
        user.setDepartment(form.getDepartment());
        user.setPosition(form.getPosition());
        user.setLocation(form.getLocation());
        user.setAddress(form.getAddress());
        user.setWorkPhone(form.getWorkPhone());
        user.setMobile(form.getMobile());
        user.setEmail(form.getEmail());
    }

    /** Reassigns the user's role. Only a Super Admin should call this. */
    public void changeRole(long userId, Role newRole) {
        AppUser user = getById(userId);
        user.setRole(newRole);
        userDao.update(user);
    }

    /**
     * Deactivates the account (sets {@code active = false}).
     * The row is kept for audit purposes; the user loses access immediately.
     */
    public void softDelete(long userId) {
        AppUser user = getById(userId);
        user.setActive(false);
        userDao.update(user);
    }

    /** Sets a new random password, forces a change on next login, returns the raw value. */
    public String regeneratePassword(long userId) {
        AppUser user = getById(userId);
        String raw = PasswordUtil.generateReadablePassword();
        user.setPasswordHash(PasswordUtil.hash(raw));
        user.setMustChangePassword(true);
        userDao.update(user);
        return raw;
    }

    /**
     * Changes the user's password to the supplied raw (unhashed) value and clears
     * the {@code mustChangePassword} flag so they are not forced to change it again.
     * Called by {@link com.company.directory.servlet.ChangePasswordServlet}.
     */
    public void changePassword(long userId, String newRawPassword) {
        AppUser user = getById(userId);
        user.setPasswordHash(PasswordUtil.hash(newRawPassword));
        user.setMustChangePassword(false);
        userDao.update(user);
    }

    /**
     * Unlocks the user's profile so they can edit it again.
     * Called by {@link EditRequestService} when an admin approves an edit request.
     */
    public void unlockProfile(long userId) {
        AppUser user = getById(userId);
        user.setProfileLocked(false);
        userDao.update(user);
    }
}
