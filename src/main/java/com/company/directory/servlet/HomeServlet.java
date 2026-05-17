package com.company.directory.servlet;

import com.company.directory.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Entry point for the application root ({@code /}).
 *
 * <p>Anonymous visitors see the public landing page.  Logged-in users are
 * redirected based on their state:
 * <ul>
 *   <li>If {@code mustChangePassword} is set → {@code /change-password}</li>
 *   <li>Otherwise → {@code /profile}</li>
 * </ul>
 *
 * <p><b>Mapping note:</b> this servlet is mapped to {@code ""} (empty string =
 * the context root only), NOT {@code "/"}.  Using {@code "/"} would replace
 * Tomcat's DefaultServlet and break static file serving.
 */
@WebServlet("")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Look up the logged-in user from the session (null if not logged in).
        HttpSession session = request.getSession(false);
        SessionUser user = (session == null)
                ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);

        if (user == null) {
            // Not logged in → show the public landing page.
            request.getRequestDispatcher("/WEB-INF/jsp/landing.jsp").forward(request, response);
        } else if (user.isMustChangePassword()) {
            // Logged in but password must be changed first.
            response.sendRedirect(request.getContextPath() + "/change-password");
        } else {
            // Logged in and ready → go to their profile.
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
}
