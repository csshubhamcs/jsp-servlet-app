package com.company.directory.servlet;

import com.company.directory.dao.AppUserDao;
import com.company.directory.dao.EditRequestDao;
import com.company.directory.model.AppUser;
import com.company.directory.model.Role;
import com.company.directory.service.EditRequestService;
import com.company.directory.service.UserService;
import com.company.directory.service.dto.ProfileForm;
import com.company.directory.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Manages a user's own profile page.
 *
 * <p>Three sub-paths are handled:
 * <ul>
 *   <li>{@code GET /profile}         — view profile; shows flash messages and edit/request buttons.</li>
 *   <li>{@code GET /profile/edit}    — profile edit form (blocked if profile is locked and not Super Admin).</li>
 *   <li>{@code GET /profile/request} — form to submit a profile-unlock request.</li>
 *   <li>{@code POST /profile/edit}   — saves profile; rules differ by role (see {@link com.company.directory.service.UserService}).</li>
 *   <li>{@code POST /profile/request}— submits an unlock request via {@link com.company.directory.service.EditRequestService}.</li>
 * </ul>
 *
 * <p>Flash messages (saved / requested / passwordChanged) use the session as a
 * one-shot store: set on POST redirect, consumed and cleared on the subsequent GET.
 * This follows the Post-Redirect-Get pattern to prevent duplicate form submissions.
 */
@WebServlet("/profile/*")
public class ProfileServlet extends HttpServlet {

    private final UserService userService = new UserService(new AppUserDao());
    private final EditRequestService editRequestService =
            new EditRequestService(new EditRequestDao(), userService);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sub = path(request);           // "" | "/edit" | "/request"
        SessionUser current = currentUser(request);
        AppUser user = userService.getById(current.getId());

        if (sub.equals("/edit")) {
            // Redirect non-Super-Admin users away if their profile is already locked.
            // (Super Admins can always edit any profile.)
            if (user.getRole() != Role.SUPER_ADMIN && user.isProfileLocked()) {
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }
            request.setAttribute("user", user);
            request.getRequestDispatcher("/WEB-INF/jsp/profile/edit.jsp").forward(request, response);

        } else if (sub.equals("/request")) {
            // Show the form for submitting a profile-unlock request.
            request.getRequestDispatcher("/WEB-INF/jsp/profile/request.jsp")
                   .forward(request, response);

        } else {
            // Default: show the profile view page.
            // Consume any one-shot flash messages set by a previous POST redirect.
            request.setAttribute("user", user);
            request.setAttribute("hasPending",
                    editRequestService.hasPendingRequest(user.getId()));
            consumeFlash(request, "flashSaved", "saved");
            consumeFlash(request, "flashRequested", "requested");
            consumeFlash(request, "flashPasswordChanged", "passwordChanged");
            request.getRequestDispatcher("/WEB-INF/jsp/profile/view.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sub = path(request);           // "/edit" | "/request"
        SessionUser current = currentUser(request);

        if (sub.equals("/edit")) {
            // 1. Read the submitted form fields.
            // 2. Decide which save path to use based on role:
            //    - Super Admin: no lock check, profile never gets locked.
            //    - Everyone else: one-time edit, then the profile locks.
            // 3. Set a flash message and redirect back to the profile view (Post-Redirect-Get).
            ProfileForm form = readProfileForm(request);
            AppUser user = userService.getById(current.getId());
            if (user.getRole() == Role.SUPER_ADMIN) {
                userService.updateProfileAsSuperAdmin(current.getId(), form);
            } else {
                userService.updateOwnProfile(current.getId(), form);
            }
            request.getSession().setAttribute("flashSaved", true);

        } else if (sub.equals("/request")) {
            // Submit a profile-unlock request; the service prevents duplicates.
            editRequestService.raiseRequest(current.getId(), request.getParameter("reason"));
            request.getSession().setAttribute("flashRequested", true);
        }

        // Always redirect back to the profile view after a POST (Post-Redirect-Get pattern).
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private ProfileForm readProfileForm(HttpServletRequest r) {
        ProfileForm f = new ProfileForm();
        f.setFullName(r.getParameter("fullName"));
        f.setEmployeeId(r.getParameter("employeeId"));
        f.setDepartment(r.getParameter("department"));
        f.setPosition(r.getParameter("position"));
        f.setLocation(r.getParameter("location"));
        f.setAddress(r.getParameter("address"));
        f.setWorkPhone(r.getParameter("workPhone"));
        f.setMobile(r.getParameter("mobile"));
        f.setEmail(r.getParameter("email"));
        return f;
    }

    private String path(HttpServletRequest request) {
        String info = request.getPathInfo();
        return (info == null) ? "" : info;
    }

    private SessionUser currentUser(HttpServletRequest request) {
        return (SessionUser) request.getSession(false).getAttribute(SessionUser.SESSION_KEY);
    }

    /** Moves a one-shot session flag into a request attribute, then clears it. */
    private void consumeFlash(HttpServletRequest request, String sessionKey, String requestKey) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(sessionKey) != null) {
            request.setAttribute(requestKey, true);
            session.removeAttribute(sessionKey);
        }
    }
}
