<%--
  admin/users.jsp — Super Admin user management table.

  Each row shows a user's name, username, role (with an inline change form),
  status (Active / Inactive), and action buttons (Reset password, Delete/Deactivate).
  "Delete" is a soft-delete: the user is deactivated, not removed from the database.

  Expected request attributes:
    users — List<AppUser> (all users, active and inactive, sorted by full name)
    roles — Role[] (all Role enum values, for the role <select>)
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4">

    <div class="page-head d-flex flex-wrap justify-content-between align-items-end gap-3 reveal">
      <div>
        <span class="page-eyebrow"><i class="bi bi-people"></i> Administration</span>
        <h3>Manage Users</h3>
        <p class="lead-sub">Create accounts, assign roles and manage access.</p>
      </div>
      <a class="btn btn-success" href="${ctx}/admin/users/new">
        <i class="bi bi-person-plus"></i> Create user
      </a>
    </div>

    <div class="table-card reveal d1">
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>Full name</th>
              <th>Username</th>
              <th>Role</th>
              <th>Status</th>
              <th class="text-end">Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="u" items="${users}">
              <tr>
                <td>
                  <div class="name-cell">
                    <span class="avatar-chip">
                      <c:choose>
                        <c:when test="${not empty u.fullName}">${fn:toUpperCase(fn:substring(u.fullName, 0, 1))}</c:when>
                        <c:otherwise>?</c:otherwise>
                      </c:choose>
                    </span>
                    <span class="cell-strong"><c:out value="${u.fullName}"/></span>
                  </div>
                </td>
                <td>
                  <span style="color:var(--ink-soft)">
                    <i class="bi bi-at"></i><c:out value="${u.username}"/>
                  </span>
                </td>
                <td>
                  <form class="d-flex align-items-center gap-1"
                        action="${ctx}/admin/users/${u.id}/role" method="post">
                    <select name="role" class="form-select form-select-sm w-auto">
                      <c:forEach var="r" items="${roles}">
                        <option value="${r}" ${r == u.role ? 'selected' : ''}><c:out value="${r}"/></option>
                      </c:forEach>
                    </select>
                    <button class="btn btn-sm btn-outline-primary" type="submit"
                            title="Apply role change">
                      <i class="bi bi-check2"></i>
                    </button>
                  </form>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${u.active}">
                      <span class="status-pill status-yes">
                        <i class="bi bi-check-circle-fill"></i> Active
                      </span>
                    </c:when>
                    <c:otherwise>
                      <span class="status-pill status-no">
                        <i class="bi bi-slash-circle-fill"></i> Inactive
                      </span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <div class="d-flex gap-2 justify-content-end flex-wrap">
                    <form class="d-inline"
                          action="${ctx}/admin/users/${u.id}/regenerate-password" method="post">
                      <button class="btn btn-sm btn-outline-warning" type="submit">
                        <i class="bi bi-arrow-repeat"></i> Reset password
                      </button>
                    </form>
                    <c:if test="${u.active}">
                      <form class="d-inline"
                            action="${ctx}/admin/users/${u.id}/delete" method="post">
                        <button class="btn btn-sm btn-outline-danger" type="submit"
                                onclick="return confirm('Deactivate this user? They will lose access immediately.');">
                          <i class="bi bi-trash3"></i> Delete
                        </button>
                      </form>
                    </c:if>
                  </div>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty users}">
              <tr>
                <td colspan="5">
                  <div class="empty-state">
                    <div class="empty-icon"><i class="bi bi-people"></i></div>
                    <h5>No users yet</h5>
                    <p>Create the first account to get started.</p>
                  </div>
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
