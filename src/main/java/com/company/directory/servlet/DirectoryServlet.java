package com.company.directory.servlet;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import com.company.directory.service.DirectoryService;
import com.company.directory.service.Page;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Serves the employee directory search page and its AJAX fragment.
 *
 * <p>Two mappings:
 * <ul>
 *   <li>{@code GET /directory}        — full page with search box and initial results.</li>
 *   <li>{@code GET /directory/search} — HTML fragment only (the results table + pagination),
 *       returned in response to the live-search XHR triggered by typing in the search box.
 *       The JavaScript in {@code app.js} swaps this fragment into the page without a full reload.</li>
 * </ul>
 *
 * <p>Both paths share the same search logic; only the JSP template differs.
 * Query parameters: {@code q} (search text) and {@code page} (zero-based page number).
 */
@WebServlet({"/directory", "/directory/search"})
public class DirectoryServlet extends HttpServlet {

    private final DirectoryService directoryService = new DirectoryService(new AppUserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Read search parameters from the URL (q = search text, page = page number).
        String query = request.getParameter("q");
        int page = parsePageParam(request.getParameter("page"));

        // 2. Run the search (handles blank query, pagination, lowercase matching).
        Page<AppUser> result = directoryService.search(query, page);
        request.setAttribute("result", result);
        request.setAttribute("query", query == null ? "" : query);

        // 3. Pick the right JSP:
        //    - /directory/search → results fragment only (swapped in by live-search XHR).
        //    - /directory        → full page (includes the search box and the results fragment).
        boolean isFragmentRequest = request.getServletPath().equals("/directory/search");
        String jsp = isFragmentRequest ? "/WEB-INF/jsp/directory/results.jsp"
                                       : "/WEB-INF/jsp/directory/list.jsp";
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    /** Parses the page URL parameter; returns 0 if it is missing or not a number. */
    private int parsePageParam(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
