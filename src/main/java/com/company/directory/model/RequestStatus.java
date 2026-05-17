package com.company.directory.model;

/**
 * Lifecycle states of a profile {@link EditRequest}.
 *
 * <p>An {@link EditRequest} starts as {@code PENDING} and is resolved to
 * either {@code APPROVED} (profile unlocked) or {@code REJECTED} (no change).
 * A resolved request cannot be re-opened.
 *
 */
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
