package com.company.directory.servlet;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import com.company.directory.service.PasswordUtil;
import com.company.directory.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles the login page.
 *
 * <p>GET  — renders the login form ({@code login.jsp}).
 * <p>POST — validates credentials; on success creates a fresh session
 *           (preventing session-fixation attacks) and redirects to {@code /}.
 *           On failure re-renders the form with a generic error message
 *           (deliberately vague to avoid username enumeration).
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AppUserDao userDao = new AppUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Read submitted credentials.
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 2. Look up the user and verify: must exist, be active, and password must match.
        AppUser user = (username == null) ? null : userDao.findByUsername(username);
        boolean credentialsValid = user != null && user.isActive()
                && PasswordUtil.matches(password == null ? "" : password, user.getPasswordHash());

        // 3a. If invalid, re-show the login form with a generic error (no hint about what was wrong).
        if (!credentialsValid) {
            request.setAttribute("error", "Invalid username or password.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        // 3b. Credentials are good: create a fresh session (prevents session fixation),
        //     store the logged-in user snapshot, and redirect to the home/dashboard page.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionUser.SESSION_KEY, new SessionUser(user));
        response.sendRedirect(request.getContextPath() + "/");
    }
}
