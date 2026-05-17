package com.company.directory.service.exception;

/**
 * Signals an expected business-rule violation.
 *
 * <p>Examples: "username already taken", "profile is locked", "request not found".
 *
 * <p>The message is always user-safe (no stack traces, no SQL details) and is
 * displayed directly in the UI.  The {@link com.company.directory.servlet.ErrorServlet}
 * detects this type and shows its message as-is; all other exceptions show a
 * generic fallback message.
 *
 * <p>Using an unchecked exception keeps service method signatures clean — callers
 * that cannot recover simply let it propagate to the error page.
 */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}
