package com.company.directory.filter;

import com.company.directory.util.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Runs on every request. Lets public paths through; otherwise requires a
 * logged-in session, enforces the role rules, and forces a first-login
 * password change.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    /**
     * Called automatically by the servlet container for every incoming request.
     *
     * <p>Checks are applied in this order — each one either lets the request
     * through or stops it immediately:
     * <ol>
     *   <li><b>Public paths</b> — /, /login, /logout, /error, /static/* are always
     *       allowed without a session.</li>
     *   <li><b>Session check</b> — any other path requires a logged-in session;
     *       missing session → redirect to /login.</li>
     *   <li><b>Forced password change</b> — if the user's {@code mustChangePassword}
     *       flag is set, only /change-password is allowed; everything else →
     *       redirect to /change-password.</li>
     *   <li><b>Role checks</b> — /directory and /requests need ADMIN or above;
     *       /admin/* needs SUPER_ADMIN. Anything else → 403 Forbidden.</li>
     *   <li><b>Allow</b> — the request reaches the servlet.</li>
     * </ol>
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Step 1: Public paths — no login required.
        if (path.equals("/") || path.equals("/login") || path.equals("/logout")
                || path.equals("/error") || path.startsWith("/static/")) {
            chain.doFilter(req, res);
            return;
        }

        // Step 2: All other paths need a valid session.
        HttpSession session = request.getSession(false);
        SessionUser user = (session == null)
                ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Step 3: Forced first-login password change — only /change-password is allowed.
        if (user.isMustChangePassword() && !path.equals("/change-password")) {
            response.sendRedirect(request.getContextPath() + "/change-password");
            return;
        }

        // Step 4: Role checks — block paths the user's role cannot access.
        if ((path.startsWith("/directory") || path.startsWith("/requests"))
                && !user.isAdminOrAbove()) {
            forbidden(request, response);
            return;
        }
        if (path.startsWith("/admin") && !user.isSuperAdmin()) {
            forbidden(request, response);
            return;
        }

        // Step 5: All checks passed — let the request reach its servlet.
        chain.doFilter(req, res);
    }

    private void forbidden(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute("message", "You do not have access to that page.");
        request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
    }
}
