package com.company.directory.servlet;

import com.company.directory.dao.AppUserDao;
import com.company.directory.service.UserService;
import com.company.directory.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles password changes for logged-in users.
 *
 * <p>This servlet serves two distinct scenarios:
 * <ol>
 *   <li><b>Forced first-login change</b> — when {@code mustChangePassword} is
 *       {@code true} the {@link com.company.directory.filter.AuthFilter} redirects
 *       every request here until the user sets a new password.  The page is
 *       rendered without the navbar so there is no way to navigate away.</li>
 *   <li><b>Voluntary change</b> — any logged-in user can visit
 *       {@code /change-password} at any time via the navbar link.</li>
 * </ol>
 *
 * <p>Validation: minimum 8 characters, passwords must match.  Errors are
 * shown inline; the session's {@code mustChangePassword} flag is cleared in
 * memory immediately after a successful change so the filter stops redirecting
 * within the same request cycle.
 */
@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final UserService userService = new UserService(new AppUserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUser user = currentUser(request);
        request.setAttribute("forced", user.isMustChangePassword());
        request.getRequestDispatcher("/WEB-INF/jsp/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Read the submitted passwords.
        SessionUser user = currentUser(request);
        String newPassword = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");

        // 2. Validate: must be at least 8 characters and both fields must match.
        String error = null;
        if (newPassword == null || newPassword.length() < 8) {
            error = "Password must be at least 8 characters.";
        } else if (!newPassword.equals(confirm)) {
            error = "Passwords do not match.";
        }

        // 3a. Validation failed — re-show the form with the error message.
        if (error != null) {
            request.setAttribute("forced", user.isMustChangePassword());
            request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/jsp/change-password.jsp")
                   .forward(request, response);
            return;
        }

        // 3b. Validation passed — save the new password, clear the forced-change flag
        //     in the session so AuthFilter stops redirecting, and show a success message.
        userService.changePassword(user.getId(), newPassword);
        user.setMustChangePassword(false);   // update session immediately (no re-login needed)
        request.getSession().setAttribute("flashPasswordChanged", true);
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
    }
}
