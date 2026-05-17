package com.company.directory.servlet;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import com.company.directory.model.Role;
import com.company.directory.service.UserService;
import com.company.directory.service.dto.CreateUserForm;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Super-Admin user management: list, create, change role, regenerate password, deactivate.
 *
 * <p>Accessible to Super Admin only (enforced by
 * {@link com.company.directory.filter.AuthFilter}).
 *
 * <p>URL structure:
 * <ul>
 *   <li>{@code GET  /admin/users}              — list all users.</li>
 *   <li>{@code GET  /admin/users/new}          — "create user" form.</li>
 *   <li>{@code POST /admin/users}              — submit new user; on success forwards to
 *                                                 {@code credentials.jsp} with the one-time password.</li>
 *   <li>{@code POST /admin/users/{id}/role}            — change role.</li>
 *   <li>{@code POST /admin/users/{id}/delete}          — deactivate account (soft delete).</li>
 *   <li>{@code POST /admin/users/{id}/regenerate-password} — generate a new random password;
 *                                                             forwards to {@code credentials.jsp}.</li>
 * </ul>
 */
@WebServlet({"/admin/users", "/admin/users/*"})
public class AdminUserServlet extends HttpServlet {

    private final AppUserDao userDao = new AppUserDao();
    private final UserService userService = new UserService(userDao);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sub = path(request);   // "" | "/new"
        if (sub.equals("/new")) {
            // Show the "create new user" form.
            request.getRequestDispatcher("/WEB-INF/jsp/admin/create-user.jsp")
                   .forward(request, response);
        } else {
            // Default: list all users (active and inactive) with role controls.
            request.setAttribute("users", userDao.findAllOrderByFullName());
            request.setAttribute("roles", Role.values());
            request.getRequestDispatcher("/WEB-INF/jsp/admin/users.jsp")
                   .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sub = path(request);   // "" | "/{id}/{action}"

        if (sub.isEmpty()) {
            // POST /admin/users — create a new user account.
            // 1. Read form fields into a CreateUserForm object.
            // 2. The service validates uniqueness, hashes the password, and saves.
            // 3. Forward to credentials.jsp to show the one-time password (never stored in plain text).
            CreateUserForm form = new CreateUserForm();
            form.setFullName(request.getParameter("fullName"));
            form.setUsername(request.getParameter("username"));
            form.setPassword(request.getParameter("password"));
            form.setRole(Role.valueOf(request.getParameter("role")));
            AppUser created = userService.createUser(form);
            request.setAttribute("heading", "Account created");
            request.setAttribute("username", created.getUsername());
            request.setAttribute("password", form.getPassword());
            request.getRequestDispatcher("/WEB-INF/jsp/admin/credentials.jsp")
                   .forward(request, response);
            return;
        }

        // POST /admin/users/{id}/{action} — act on an existing user.
        // Parse the numeric id and the action name from the path.
        long id = idFromPath(sub);
        String action = sub.substring(sub.lastIndexOf('/') + 1);

        switch (action) {
            case "role" ->
                // Change the user's role to the value chosen in the dropdown.
                userService.changeRole(id, Role.valueOf(request.getParameter("role")));

            case "delete" ->
                // Soft-delete: sets active=false. The row stays in the DB for audit purposes.
                userService.softDelete(id);

            case "regenerate-password" -> {
                // Generate a new random password, save it (hashed), and show it once
                // on the credentials page. Fetch the username first for display.
                String username = userService.getById(id).getUsername();
                String newPassword = userService.regeneratePassword(id);
                request.setAttribute("heading", "Password regenerated");
                request.setAttribute("username", username);
                request.setAttribute("password", newPassword);
                request.getRequestDispatcher("/WEB-INF/jsp/admin/credentials.jsp")
                       .forward(request, response);
                return;
            }

            default -> { /* Unknown action — fall through to redirect safely. */ }
        }

        // Redirect back to the user list after role change or delete (Post-Redirect-Get).
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private String path(HttpServletRequest request) {
        String info = request.getPathInfo();
        return (info == null) ? "" : info;
    }

    /** From "/12/role" returns 12. */
    private long idFromPath(String sub) {
        String[] parts = sub.split("/");   // ["", "12", "role"]
        return Long.parseLong(parts[1]);
    }
}
