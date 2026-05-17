<%--
  change-password.jsp — password change form; used in two modes:
    1. Forced first-login (forced=true): no navbar, no footer — the user cannot
       navigate anywhere until they set a new password.
    2. Voluntary change (forced=false): shows the navbar and the standard footer.

  Note: the footer is inlined conditionally here rather than using foot.jspf
  because foot.jspf always includes the footer. When forced=true we intentionally
  omit the footer to keep the page completely locked down.
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell ${forced ? 'bg-aurora' : 'bg-app'}">

<c:if test="${!forced}">
  <%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
</c:if>

<main class="app-main d-flex align-items-center" style="padding:5vh 0;">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-12 col-sm-10 col-md-7 col-lg-5">
        <div class="auth-card reveal">
          <div class="auth-logo">
            <i class="bi bi-shield-lock-fill"></i>
          </div>
          <h3 class="mb-1">Change password</h3>
          <p class="text-muted-2 mb-4">
            Choose a strong password — at least 8 characters.
          </p>

          <c:if test="${forced}">
            <div class="alert alert-warning mb-3">
              <i class="bi bi-exclamation-triangle-fill"></i>
              <span>
                <strong>Action required.</strong>
                You must set a new password before you can continue using your account.
              </span>
            </div>
          </c:if>
          <c:if test="${not empty error}">
            <div class="alert alert-danger mb-3">
              <i class="bi bi-exclamation-octagon-fill"></i>
              <span><c:out value="${error}"/></span>
            </div>
          </c:if>

          <form action="${ctx}/change-password" method="post">
            <div class="mb-3">
              <label class="form-label" for="newPassword">New password</label>
              <div class="input-affix">
                <i class="bi bi-key ic"></i>
                <input class="form-control" id="newPassword" type="password"
                       name="newPassword" required minlength="8"
                       autocomplete="new-password" placeholder="At least 8 characters"/>
                <button class="input-affix-btn" type="button"
                        data-toggle-password="newPassword"
                        aria-label="Show password" title="Show password">
                  <i class="bi bi-eye"></i>
                </button>
              </div>
            </div>
            <div class="mb-4">
              <label class="form-label" for="confirmPassword">Confirm new password</label>
              <div class="input-affix">
                <i class="bi bi-key-fill ic"></i>
                <input class="form-control" id="confirmPassword" type="password"
                       name="confirmPassword" required minlength="8"
                       autocomplete="new-password" placeholder="Re-enter your new password"/>
                <button class="input-affix-btn" type="button"
                        data-toggle-password="confirmPassword"
                        aria-label="Show password" title="Show password">
                  <i class="bi bi-eye"></i>
                </button>
              </div>
              <p id="pwMatchFeedback" class="pw-match-msg" aria-live="polite"></p>
            </div>
            <div class="d-flex gap-2">
              <button id="changePwSubmit" class="btn btn-primary flex-grow-1" type="submit">
                <i class="bi bi-check2-circle"></i> Update password
              </button>
              <c:if test="${!forced}">
                <a class="btn btn-secondary" href="${ctx}/profile">Cancel</a>
              </c:if>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</main>

<script src="${pageContext.request.contextPath}/static/js/app.js"></script>
<c:if test="${!forced}">
<footer class="app-footer">
  <div class="container d-flex flex-wrap justify-content-between gap-2">
    <span><span class="brand-mark sm"><i class="bi bi-telephone-fill"></i></span>
      Employee Telephone Directory</span>
    <span class="text-muted-2">Role-based contact management</span>
  </div>
</footer>
</c:if>
</body>
</html>
