package com.company.directory.servlet;

import com.company.directory.dao.AppUserDao;
import com.company.directory.dao.EditRequestDao;
import com.company.directory.service.EditRequestService;
import com.company.directory.service.UserService;
import com.company.directory.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles the admin's profile-unlock request queue.
 *
 * <p>Accessible to Admin and Super Admin only (enforced by
 * {@link com.company.directory.filter.AuthFilter}).
 *
 * <p>Two mappings:
 * <ul>
 *   <li>{@code GET /requests}                    — lists all pending requests.</li>
 *   <li>{@code POST /requests/{id}/approve}      — approves the request (unlocks the profile).</li>
 *   <li>{@code POST /requests/{id}/reject}       — rejects the request (profile stays locked).</li>
 * </ul>
 *
 * <p>The POST URL carries the request id and action as path segments, which is
 * a clean alternative to hidden form fields.
 */
@WebServlet({"/requests", "/requests/*"})
public class EditRequestServlet extends HttpServlet {

    private final EditRequestService editRequestService =
            new EditRequestService(new EditRequestDao(), new UserService(new AppUserDao()));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Load all pending requests and forward to the list view.
        request.setAttribute("requests", editRequestService.pendingRequests());
        request.getRequestDispatcher("/WEB-INF/jsp/requests/list.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 1. Parse the path info to get the request id and action.
        //    Expected format: /{id}/approve  or  /{id}/reject
        //    e.g. /requests/42/approve → pathInfo = "/42/approve" → parts = ["", "42", "approve"]
        String sub = request.getPathInfo();
        String[] parts = (sub == null) ? new String[0] : sub.split("/");
        if (parts.length < 3) {
            // Path is malformed or missing the id/action — ignore and redirect to the queue.
            response.sendRedirect(request.getContextPath() + "/requests");
            return;
        }

        long requestId;
        try {
            requestId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            // The id segment is not a number — redirect safely.
            response.sendRedirect(request.getContextPath() + "/requests");
            return;
        }

        // 2. Identify the admin who is taking the action.
        String action = parts[2];
        long resolverId = ((SessionUser) request.getSession(false)
                .getAttribute(SessionUser.SESSION_KEY)).getId();

        // 3. Approve or reject the request; the service validates status and updates the DB.
        if (action.equals("approve")) {
            editRequestService.approve(requestId, resolverId);
        } else if (action.equals("reject")) {
            editRequestService.reject(requestId, resolverId);
        }

        // 4. Redirect back to the queue (Post-Redirect-Get).
        response.sendRedirect(request.getContextPath() + "/requests");
    }
}
