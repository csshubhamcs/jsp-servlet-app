<%--
  admin/create-user.jsp — form for creating a new user account.

  The "Generate" button calls genPassword() (defined in app.js) to fill the
  password field with a random readable password from the server-side charset.

  On success AdminUserServlet forwards to credentials.jsp to display the
  one-time credentials. The password is never retrievable after that page.

  Expected request attribute (only on validation failure):
    error — String (user-safe error message, e.g. "That username is already taken.")
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4" style="max-width:600px;">

    <div class="page-head reveal">
      <span class="page-eyebrow"><i class="bi bi-person-plus"></i> New account</span>
      <h3>Create User</h3>
      <p class="lead-sub">Set up an account and share the one-time credentials.</p>
    </div>

    <c:if test="${not empty error}">
      <div class="alert alert-danger mb-3 reveal d1">
        <i class="bi bi-exclamation-octagon-fill"></i>
        <span><c:out value="${error}"/></span>
      </div>
    </c:if>

    <div class="panel p-4 reveal d2">
      <form action="${ctx}/admin/users" method="post">
        <div class="mb-3">
          <label class="form-label" for="fullName">Full name</label>
          <input class="form-control" id="fullName" type="text" name="fullName"
                 required maxlength="120" placeholder="e.g. Jane Anderson"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="username">Username</label>
          <input class="form-control" id="username" type="text" name="username"
                 required minlength="3" maxlength="60" autocomplete="off"
                 placeholder="e.g. jane.anderson"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="password">Password</label>
          <div class="input-group">
            <input class="form-control" type="password" name="password" id="password"
                   required minlength="8" maxlength="60" autocomplete="off"
                   placeholder="At least 8 characters"/>
            <button class="btn btn-outline-secondary" type="button"
                    data-toggle-password="password"
                    aria-label="Show password" title="Show password">
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-outline-secondary" type="button"
                    onclick="genPassword('password')">
              <i class="bi bi-shuffle"></i> Generate
            </button>
          </div>
          <p class="mt-1 mb-0" style="color:var(--muted);font-size:.8rem">
            <i class="bi bi-info-circle"></i>
            Generate a strong 14-character password, or type your own.
          </p>
        </div>
        <div class="mb-3">
          <label class="form-label" for="role">Role</label>
          <select class="form-select" id="role" name="role">
            <option value="USER" selected>User</option>
            <option value="ADMIN">Admin</option>
            <option value="SUPER_ADMIN">Super Admin</option>
          </select>
        </div>

        <hr class="divider-soft"/>
        <div class="d-flex gap-2 flex-wrap">
          <button class="btn btn-primary" type="submit">
            <i class="bi bi-person-check"></i> Create user
          </button>
          <a class="btn btn-secondary" href="${ctx}/admin/users">
            <i class="bi bi-x-lg"></i> Cancel
          </a>
        </div>
      </form>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
