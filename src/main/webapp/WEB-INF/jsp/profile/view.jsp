<%--
  profile/view.jsp — displays the logged-in user's profile.

  Flash messages (one-shot, consumed from session by the servlet):
    saved           — shown after a successful profile save
    requested       — shown after submitting an edit request
    passwordChanged — shown after a successful password change

  Expected request attributes:
    user       — AppUser (the current user's full record)
    hasPending — boolean (true if the user already has a pending edit request)
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4" style="max-width:880px;">

    <div class="page-head reveal">
      <span class="page-eyebrow"><i class="bi bi-person-badge"></i> Personal record</span>
      <h3>My Profile</h3>
      <p class="lead-sub">Your contact details as they appear in the company directory.</p>
    </div>

    <c:if test="${saved}">
      <div class="alert alert-success mb-3 reveal d1">
        <i class="bi bi-check-circle-fill"></i>
        <span>Your profile has been saved successfully.</span>
      </div>
    </c:if>
    <c:if test="${requested}">
      <div class="alert alert-success mb-3 reveal d1">
        <i class="bi bi-send-check-fill"></i>
        <span>Your edit request has been submitted for approval.</span>
      </div>
    </c:if>
    <c:if test="${passwordChanged}">
      <div class="alert alert-success mb-3 reveal d1">
        <i class="bi bi-shield-check"></i>
        <span>Your password has been updated.</span>
      </div>
    </c:if>

    <c:if test="${user.role != 'SUPER_ADMIN' && !user.profileLocked}">
      <div class="alert alert-danger mb-3 reveal d1">
        <i class="bi bi-exclamation-triangle-fill"></i>
        <span>
          <strong>Heads up — you can edit your profile only once.</strong>
          After you save your changes, your profile will be locked and further
          edits will require administrator approval.
        </span>
      </div>
    </c:if>

    <div class="panel overflow-hidden reveal d2">
      <!-- Banner -->
      <div class="profile-banner">
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <div class="profile-avatar">
            <c:choose>
              <c:when test="${not empty user.fullName}">${fn:toUpperCase(fn:substring(user.fullName, 0, 1))}</c:when>
              <c:otherwise>?</c:otherwise>
            </c:choose>
          </div>
          <div>
            <h2 class="mb-1" style="color:#fff"><c:out value="${user.fullName}"/></h2>
            <div class="d-flex align-items-center gap-2 flex-wrap">
              <span style="color:rgba(255,255,255,.85);font-weight:600">
                <i class="bi bi-at"></i> <c:out value="${user.username}"/>
              </span>
              <span class="role-badge" style="background:rgba(255,255,255,.22);color:#fff">
                <i class="bi bi-shield-fill"></i>
                <c:out value="${user.role}"/>
              </span>
              <c:if test="${user.profileLocked}">
                <span class="status-pill" style="background:rgba(255,255,255,.22);color:#fff">
                  <i class="bi bi-lock-fill"></i> Locked
                </span>
              </c:if>
            </div>
          </div>
        </div>
      </div>

      <!-- Detail fields -->
      <div class="profile-body">
        <div class="card-surface p-4">
          <div class="field-grid">
            <div class="field-row">
              <span class="f-label"><i class="bi bi-person"></i> Full name</span>
              <span class="f-value ${empty user.fullName ? 'empty' : ''}">
                <c:out value="${empty user.fullName ? 'Not provided' : user.fullName}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-hash"></i> Employee ID</span>
              <span class="f-value ${empty user.employeeId ? 'empty' : ''}">
                <c:out value="${empty user.employeeId ? 'Not provided' : user.employeeId}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-building"></i> Department</span>
              <span class="f-value ${empty user.department ? 'empty' : ''}">
                <c:out value="${empty user.department ? 'Not provided' : user.department}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-briefcase"></i> Position</span>
              <span class="f-value ${empty user.position ? 'empty' : ''}">
                <c:out value="${empty user.position ? 'Not provided' : user.position}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-geo-alt"></i> Location</span>
              <span class="f-value ${empty user.location ? 'empty' : ''}">
                <c:out value="${empty user.location ? 'Not provided' : user.location}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-pin-map"></i> Address</span>
              <span class="f-value ${empty user.address ? 'empty' : ''}">
                <c:out value="${empty user.address ? 'Not provided' : user.address}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-telephone"></i> Work phone</span>
              <span class="f-value ${empty user.workPhone ? 'empty' : ''}">
                <c:out value="${empty user.workPhone ? 'Not provided' : user.workPhone}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-phone"></i> Mobile</span>
              <span class="f-value ${empty user.mobile ? 'empty' : ''}">
                <c:out value="${empty user.mobile ? 'Not provided' : user.mobile}"/>
              </span>
            </div>
            <div class="field-row">
              <span class="f-label"><i class="bi bi-envelope"></i> Email</span>
              <span class="f-value ${empty user.email ? 'empty' : ''}">
                <c:out value="${empty user.email ? 'Not provided' : user.email}"/>
              </span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="mt-4">
          <c:if test="${user.role == 'SUPER_ADMIN' || !user.profileLocked}">
            <div>
              <a class="btn btn-primary" href="${ctx}/profile/edit">
                <i class="bi bi-pencil-square"></i> Edit profile
              </a>
            </div>
          </c:if>
          <c:if test="${user.role != 'SUPER_ADMIN' && user.profileLocked}">
            <div>
              <div class="alert alert-warning mb-3">
                <i class="bi bi-lock-fill"></i>
                <span>
                  Your profile is locked. To make further changes, send a request
                  to an administrator for approval.
                </span>
              </div>
              <a class="btn btn-warning ${hasPending ? 'disabled' : ''}" href="${ctx}/profile/request">
                <i class="bi bi-pencil-square"></i> Request edit
              </a>
              <c:if test="${hasPending}">
                <span class="ms-2" style="color:var(--muted);font-size:.88rem">
                  <i class="bi bi-hourglass-split"></i>
                  A request is already pending review.
                </span>
              </c:if>
            </div>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
