package com.company.directory.service;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import com.company.directory.model.Role;
import com.company.directory.service.dto.CreateUserForm;
import com.company.directory.service.dto.ProfileForm;
import com.company.directory.service.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock AppUserDao dao;
    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(dao);
        lenient().when(dao.insert(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));
    }

    private AppUser user(long id, Role role) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setUsername("jdoe");
        u.setRole(role);
        u.setFullName("J Doe");
        u.setActive(true);
        return u;
    }

    @Test
    void createUserHashesPasswordAndSetsFlags() {
        when(dao.existsByUsername("jdoe")).thenReturn(false);
        CreateUserForm form = new CreateUserForm();
        form.setFullName("J Doe");
        form.setUsername("jdoe");
        form.setPassword("Plain123!");
        form.setRole(Role.USER);

        AppUser created = service.createUser(form);

        assertNotEquals("Plain123!", created.getPasswordHash());
        assertTrue(PasswordUtil.matches("Plain123!", created.getPasswordHash()));
        assertTrue(created.isMustChangePassword());
        assertFalse(created.isProfileLocked());
        assertEquals(Role.USER, created.getRole());
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(dao.existsByUsername("jdoe")).thenReturn(true);
        CreateUserForm form = new CreateUserForm();
        form.setFullName("J Doe");
        form.setUsername("jdoe");
        form.setPassword("Plain123!");
        form.setRole(Role.USER);

        assertThrows(AppException.class, () -> service.createUser(form));
        verify(dao, never()).insert(any());
    }

    @Test
    void firstProfileSaveLocksTheProfile() {
        AppUser u = user(7L, Role.USER);
        when(dao.findById(7L)).thenReturn(u);
        ProfileForm form = new ProfileForm();
        form.setFullName("Jane Doe");
        form.setDepartment("Sales");

        service.updateOwnProfile(7L, form);

        assertEquals("Sales", u.getDepartment());
        assertTrue(u.isProfileCompleted());
        assertTrue(u.isProfileLocked());
        verify(dao).update(u);
    }

    @Test
    void secondProfileSaveIsRejectedWhenLocked() {
        AppUser u = user(7L, Role.USER);
        u.setProfileLocked(true);
        when(dao.findById(7L)).thenReturn(u);

        assertThrows(AppException.class, () -> service.updateOwnProfile(7L, new ProfileForm()));
    }

    @Test
    void superAdminProfileEditIgnoresLock() {
        AppUser u = user(7L, Role.USER);
        u.setProfileLocked(true);
        when(dao.findById(7L)).thenReturn(u);
        ProfileForm form = new ProfileForm();
        form.setFullName("Edited");
        form.setLocation("HQ");

        service.updateProfileAsSuperAdmin(7L, form);

        assertEquals("HQ", u.getLocation());
        assertTrue(u.isProfileLocked());
        verify(dao).update(u);
    }

    @Test
    void changeRoleUpdatesRole() {
        AppUser u = user(7L, Role.USER);
        when(dao.findById(7L)).thenReturn(u);
        service.changeRole(7L, Role.ADMIN);
        assertEquals(Role.ADMIN, u.getRole());
        verify(dao).update(u);
    }

    @Test
    void softDeleteDeactivatesUser() {
        AppUser u = user(7L, Role.USER);
        when(dao.findById(7L)).thenReturn(u);
        service.softDelete(7L);
        assertFalse(u.isActive());
        verify(dao).update(u);
    }

    @Test
    void regeneratePasswordReturnsRawAndForcesChange() {
        AppUser u = user(7L, Role.USER);
        u.setMustChangePassword(false);
        when(dao.findById(7L)).thenReturn(u);

        String raw = service.regeneratePassword(7L);

        assertNotNull(raw);
        assertTrue(PasswordUtil.matches(raw, u.getPasswordHash()));
        assertTrue(u.isMustChangePassword());
        verify(dao).update(u);
    }

    @Test
    void changePasswordClearsMustChangeFlag() {
        AppUser u = user(7L, Role.USER);
        u.setMustChangePassword(true);
        when(dao.findById(7L)).thenReturn(u);

        service.changePassword(7L, "BrandNew123");

        assertTrue(PasswordUtil.matches("BrandNew123", u.getPasswordHash()));
        assertFalse(u.isMustChangePassword());
        verify(dao).update(u);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(dao.findById(99L)).thenReturn(null);
        assertThrows(AppException.class, () -> service.getById(99L));
    }
}
