package com.company.directory.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Renders the application error page.
 *
 * <p>Registered in {@code web.xml} as the handler for HTTP 404, 500, and any
 * uncaught {@link Throwable}.  The container populates standard request attributes
 * ({@code jakarta.servlet.error.status_code}, {@code jakarta.servlet.error.exception})
 * before forwarding here.
 *
 * <p>If the exception is an {@link com.company.directory.service.exception.AppException}
 * its message is shown directly (it is guaranteed user-safe).  For all other
 * exceptions a generic message is used to avoid leaking internal details.
 *
 * <p>The {@code AuthFilter} also sets a {@code message} attribute and forwards
 * directly to {@code error.jsp} for 403 Forbidden responses; in that case this
 * servlet is not involved.
 */
@WebServlet("/error")
public class ErrorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // The container sets these standard attributes when routing an error here.
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object thrown = request.getAttribute("jakarta.servlet.error.exception");

        // If 'message' is already set (e.g. AuthFilter set it for a 403), use it as-is.
        // Otherwise, pick a safe message based on what we know about the error.
        if (request.getAttribute("message") == null) {
            String message = "Something went wrong. Please try again.";
            if (thrown instanceof com.company.directory.service.exception.AppException ae) {
                // AppException messages are guaranteed user-safe — show them directly.
                message = ae.getMessage();
            } else if ("404".equals(String.valueOf(status))) {
                message = "The page you were looking for was not found.";
            }
            request.setAttribute("message", message);
        }
        request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
    }
}
