package com.company.directory.model;

/**
 * The three account roles, in ascending order of privilege.
 *
 * <ul>
 *   <li>{@code USER}        — can view and edit their own profile only.</li>
 *   <li>{@code ADMIN}       — can also browse the directory and approve edit requests.</li>
 *   <li>{@code SUPER_ADMIN} — can also manage (create, delete, change role) all user accounts.</li>
 * </ul>
 *
 * Access rules are enforced in {@link com.company.directory.filter.AuthFilter}.
 */
public enum Role {
    USER,
    ADMIN,
    SUPER_ADMIN
}
