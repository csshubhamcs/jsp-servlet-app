<%--
  credentials.jsp — one-time credentials display shown after creating a user
  or regenerating a password.

  The password is shown in plain text exactly once. There is no database route
  back to the raw value — it is only available in the servlet's local variable
  at the moment it is passed to this page. Admins must copy it before navigating away.

  Expected request attributes:
    heading  — page title (e.g. "Account created" or "Password regenerated")
    username — the account's username
    password — the raw (unhashed) password (available only at this moment)
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4" style="max-width:560px;">

    <div class="panel overflow-hidden reveal">
      <div class="profile-banner" style="padding:1.8rem 2rem">
        <div class="d-flex align-items-center gap-3">
          <div class="auth-logo m-0" style="background:rgba(255,255,255,.2);box-shadow:none">
            <i class="bi bi-key-fill"></i>
          </div>
          <div>
            <h3 class="mb-1" style="color:#fff"><c:out value="${heading}"/></h3>
            <p class="mb-0" style="color:rgba(255,255,255,.85);font-size:.9rem">
              One-time credentials
            </p>
          </div>
        </div>
      </div>

      <div class="profile-body">
        <div class="card-surface p-4">
          <div class="alert alert-warning mb-4">
            <i class="bi bi-exclamation-triangle-fill"></i>
            <span>
              <strong>Share these now.</strong>
              For security, the password will <u>not</u> be shown again.
              Make sure you copy it before leaving this page.
            </span>
          </div>

          <div class="field-row" style="border-bottom:1px dashed var(--line)">
            <span class="f-label"><i class="bi bi-person"></i> Username</span>
            <span class="f-value" id="credUser"><c:out value="${username}"/></span>
          </div>
          <div class="field-row" style="border-bottom:none">
            <span class="f-label"><i class="bi bi-key"></i> Password</span>
            <code id="credPass"
                  style="font-size:1.1rem;font-weight:700;color:var(--brand-700);background:var(--brand-50);padding:.35rem .7rem;border-radius:9px;align-self:flex-start">
              <c:out value="${password}"/>
            </code>
          </div>

          <hr class="divider-soft"/>
          <button class="btn btn-primary w-100" type="button"
                  data-credentials="Username: ${fn:escapeXml(username)}&#10;Password: ${fn:escapeXml(password)}"
                  onclick="copyText(this.getAttribute('data-credentials'), this)">
            <i class="bi bi-clipboard-check"></i> Copy username &amp; password
          </button>
        </div>

        <div class="mt-4">
          <a class="btn btn-primary" href="${ctx}/admin/users">
            <i class="bi bi-arrow-left"></i> Back to users
          </a>
        </div>
      </div>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
