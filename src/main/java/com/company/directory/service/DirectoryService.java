package com.company.directory.service;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;

import java.util.List;

/**
 * Search and pagination logic for the employee directory.
 *
 * <p>Searches active users by name, department, or phone number.
 * Results are returned as a {@link Page} so the view can draw prev/next links.
 *
 * <p>The search term is normalised to lowercase before hitting the DAO
 * (the SQL uses {@code LOWER()} on the column side, so case is always ignored).
 */
public class DirectoryService {

    /** Rows per page. */
    public static final int PAGE_SIZE = 20;

    private final AppUserDao userDao;

    public DirectoryService(AppUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * @param query optional search text (null/blank = everyone)
     * @param page  zero-based page number (negative is clamped to 0)
     */
    public Page<AppUser> search(String query, int page) {
        String term = (query == null) ? "" : query.trim().toLowerCase();
        int pageNumber = Math.max(page, 0);
        long total = userDao.countActiveMatching(term);
        List<AppUser> items = userDao.searchActive(term, pageNumber * PAGE_SIZE, PAGE_SIZE);
        return new Page<>(items, pageNumber, PAGE_SIZE, total);
    }
}
