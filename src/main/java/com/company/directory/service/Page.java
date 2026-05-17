package com.company.directory.service;

import java.util.List;

/**
 * A slice of a larger result set, carrying everything the view needs to
 * render the current page and draw pagination controls.
 *
 * <p>Page numbers are <b>zero-based</b> internally (consistent with SQL OFFSET
 * arithmetic) but the view adds 1 when displaying them to the user.
 *
 * @param <T> the type of item in this page (e.g. {@link com.company.directory.model.AppUser})
 */
public class Page<T> {

    private final List<T> items;
    private final int pageNumber;   // zero-based
    private final int pageSize;
    private final long totalItems;

    public Page(List<T> items, int pageNumber, int pageSize, long totalItems) {
        this.items = items;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getItems() { return items; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }

    /**
     * Returns the total number of pages, always at least 1 (so the view never
     * has to handle a "0 pages" edge case).
     *
     * <p>The formula {@code (total + pageSize - 1) / pageSize} is integer ceiling
     * division — it rounds up so a partial last page is counted correctly.
     * Example: 21 items with pageSize=20 → 2 pages, not 1.
     */
    public int getTotalPages() {
        if (totalItems == 0) return 1;
        return (int) ((totalItems + pageSize - 1) / pageSize);
    }
    public boolean isFirst() { return pageNumber <= 0; }
    public boolean isLast() { return pageNumber >= getTotalPages() - 1; }
    public boolean hasPrevious() { return !isFirst(); }
    public boolean hasNext() { return !isLast(); }
}
