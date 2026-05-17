<%--
  results.jsp — HTML fragment for the directory search results table and pagination.

  This file is rendered in two contexts:
    1. Included directly inside list.jsp on the initial full-page load.
    2. Returned as a bare HTML fragment by GET /directory/search in response to
       the live-search XHR in app.js, which swaps it into the #directory-results div.

  Expected request attributes:
    result  — Page<AppUser> (items, pageNumber, totalItems, totalPages, etc.)
    query   — current search text (may be empty)
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="results-meta">
  <i class="bi bi-people-fill"></i> <strong>${result.totalItems}</strong>
  employee<c:if test="${result.totalItems != 1}">s</c:if>
  <c:if test="${not empty query}">
    matching <strong>&ldquo;<c:out value="${query}"/>&rdquo;</strong>
  </c:if>
</div>
<c:choose>
  <c:when test="${not empty result.items}">
    <div class="table-card reveal d2">
      <div class="table-responsive">
        <table class="table align-middle">
          <thead><tr>
            <th>Name</th><th>Department</th><th>Position</th><th>Location</th>
            <th>Work phone</th><th>Mobile</th><th>Email</th>
          </tr></thead>
          <tbody>
            <c:forEach var="e" items="${result.items}">
              <tr>
                <td>
                  <div class="name-cell">
                    <span class="avatar-chip">
                      <c:choose>
                        <c:when test="${not empty e.fullName}">${fn:toUpperCase(fn:substring(e.fullName, 0, 1))}</c:when>
                        <c:otherwise>?</c:otherwise>
                      </c:choose>
                    </span>
                    <span class="cell-strong"><c:out value="${e.fullName}"/></span>
                  </div>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty e.department}"><c:out value="${e.department}"/></c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty e.position}"><c:out value="${e.position}"/></c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty e.location}">
                      <i class="bi bi-geo-alt text-muted-2"></i> <c:out value="${e.location}"/>
                    </c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty e.workPhone}">
                      <a href="tel:${fn:escapeXml(e.workPhone)}"><c:out value="${e.workPhone}"/></a>
                    </c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty e.mobile}">
                      <a href="tel:${fn:escapeXml(e.mobile)}"><c:out value="${e.mobile}"/></a>
                    </c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${not empty e.email}">
                      <a href="mailto:${fn:escapeXml(e.email)}"><c:out value="${e.email}"/></a>
                    </c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-card reveal d2">
      <div class="empty-state">
        <div class="empty-icon"><i class="bi bi-person-x"></i></div>
        <h5>No employees found</h5>
        <c:choose>
          <c:when test="${not empty query}">
            <p>No one matches your search. Try a different name or clear the filter.</p>
          </c:when>
          <c:otherwise>
            <p>There are no active employees in the directory yet.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </c:otherwise>
</c:choose>
<c:if test="${result.totalPages > 1}">
  <nav class="d-flex align-items-center justify-content-center gap-3 mt-3 reveal d3">
    <c:choose>
      <c:when test="${not result.first}">
        <button type="button" class="btn btn-secondary btn-sm" data-page="${result.pageNumber - 1}">
          <i class="bi bi-chevron-left"></i> Previous</button>
      </c:when>
      <c:otherwise>
        <button type="button" class="btn btn-secondary btn-sm" disabled>
          <i class="bi bi-chevron-left"></i> Previous</button>
      </c:otherwise>
    </c:choose>
    <span style="color:var(--muted);font-size:.9rem;font-weight:700">
      Page <strong style="color:var(--ink)">${result.pageNumber + 1}</strong>
      of <strong style="color:var(--ink)">${result.totalPages}</strong>
    </span>
    <c:choose>
      <c:when test="${not result.last}">
        <button type="button" class="btn btn-secondary btn-sm" data-page="${result.pageNumber + 1}">
          Next <i class="bi bi-chevron-right"></i></button>
      </c:when>
      <c:otherwise>
        <button type="button" class="btn btn-secondary btn-sm" disabled>
          Next <i class="bi bi-chevron-right"></i></button>
      </c:otherwise>
    </c:choose>
  </nav>
</c:if>
