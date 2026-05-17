<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-aurora">

<main class="app-main d-flex align-items-center" style="padding:5vh 0;">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-12 col-sm-9 col-md-6 col-lg-5 col-xl-4">
        <div class="auth-card reveal">
          <div class="auth-logo"><i class="bi bi-telephone-fill"></i></div>
          <h3 class="mb-1">Welcome back</h3>
          <p class="text-muted-2 mb-4">Sign in to access the employee directory.</p>

          <c:if test="${not empty error}">
            <div class="alert alert-danger mb-3">
              <i class="bi bi-exclamation-octagon-fill"></i>
              <span><c:out value="${error}"/></span>
            </div>
          </c:if>
          <c:if test="${not empty param.logout}">
            <div class="alert alert-info mb-3">
              <i class="bi bi-check-circle-fill"></i>
              <span>You have been signed out successfully.</span>
            </div>
          </c:if>

          <form action="${ctx}/login" method="post">
            <div class="mb-3">
              <label class="form-label" for="username">Username</label>
              <div class="input-affix">
                <i class="bi bi-person ic"></i>
                <input class="form-control" id="username" type="text"
                       name="username" required autofocus
                       autocomplete="username" placeholder="your.username"/>
              </div>
            </div>
            <div class="mb-4">
              <label class="form-label" for="password">Password</label>
              <div class="input-affix">
                <i class="bi bi-lock ic"></i>
                <input class="form-control" id="password" type="password"
                       name="password" required
                       autocomplete="current-password" placeholder="••••••••"/>
                <button class="input-affix-btn" type="button"
                        data-toggle-password="password"
                        aria-label="Show password" title="Show password">
                  <i class="bi bi-eye"></i>
                </button>
              </div>
            </div>
            <button class="btn btn-primary w-100 btn-lg" type="submit">
              <i class="bi bi-box-arrow-in-right"></i> Sign in
            </button>
          </form>

          <hr class="divider-soft"/>
          <p class="text-center mb-0" style="color:var(--muted);font-size:.85rem">
            <i class="bi bi-shield-check"></i>
            New credentials are provided by your administrator.
          </p>
        </div>
        <p class="text-center mt-3 reveal d2">
          <a href="${ctx}/" style="color:var(--ink-soft);font-weight:600;font-size:.9rem">
            <i class="bi bi-arrow-left"></i> Back to home
          </a>
        </p>
      </div>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
