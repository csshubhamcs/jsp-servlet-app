<%--
  profile/edit.jsp — profile edit form.

  For normal users (USER, ADMIN) this is a one-time edit: saving locks the profile.
  For Super Admins it can be saved any number of times with no lock.
  The one-time-edit warning is hidden for Super Admins.

  Expected request attribute: user (AppUser)
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4" style="max-width:720px;">

    <div class="page-head reveal">
      <span class="page-eyebrow"><i class="bi bi-pencil-square"></i> Update details</span>
      <h3>Edit Profile</h3>
      <p class="lead-sub">Keep your directory information accurate and up to date.</p>
    </div>

    <%-- Only non-Super-Admin users are subject to the one-time edit lock. --%>
    <c:if test="${user.role != 'SUPER_ADMIN'}">
    <div class="alert alert-warning mb-3 reveal d1">
      <i class="bi bi-exclamation-triangle-fill"></i>
      <span>
        <strong>One-time edit.</strong>
        You can edit your profile only once. After you save, it will be locked —
        further changes will need administrator approval.
      </span>
    </div>
    </c:if>

    <div class="panel p-4 p-md-4 reveal d2">
      <form action="${ctx}/profile/edit" method="post">
        <div class="row g-3">
          <div class="col-12">
            <label class="form-label" for="fullName">Full name</label>
            <input class="form-control" id="fullName" type="text" name="fullName"
                   value="${fn:escapeXml(user.fullName)}" required
                   placeholder="e.g. Jane Anderson"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="employeeId">Employee ID</label>
            <input class="form-control" id="employeeId" type="text" name="employeeId"
                   value="${fn:escapeXml(user.employeeId)}"
                   placeholder="e.g. EMP-1042"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="department">Department</label>
            <input class="form-control" id="department" type="text" name="department"
                   value="${fn:escapeXml(user.department)}"
                   placeholder="e.g. Engineering"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="position">Position</label>
            <input class="form-control" id="position" type="text" name="position"
                   value="${fn:escapeXml(user.position)}"
                   placeholder="e.g. Senior Developer"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="location">Location</label>
            <input class="form-control" id="location" type="text" name="location"
                   value="${fn:escapeXml(user.location)}"
                   placeholder="e.g. London HQ"/>
          </div>
          <div class="col-12">
            <label class="form-label" for="address">Address</label>
            <input class="form-control" id="address" type="text" name="address"
                   value="${fn:escapeXml(user.address)}"
                   placeholder="Street, city, postcode"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="workPhone">Work phone</label>
            <input class="form-control" id="workPhone" type="text" name="workPhone"
                   value="${fn:escapeXml(user.workPhone)}"
                   placeholder="e.g. +44 20 7946 0000"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="mobile">Mobile</label>
            <input class="form-control" id="mobile" type="text" name="mobile"
                   value="${fn:escapeXml(user.mobile)}"
                   placeholder="e.g. +44 7700 900000"/>
          </div>
          <div class="col-12">
            <label class="form-label" for="email">Email</label>
            <input class="form-control" id="email" type="email" name="email"
                   value="${fn:escapeXml(user.email)}"
                   placeholder="name@company.com"/>
          </div>
        </div>

        <hr class="divider-soft"/>
        <div class="d-flex gap-2 flex-wrap">
          <button class="btn btn-primary" type="submit">
            <i class="bi bi-check2-circle"></i> Save profile
          </button>
          <a class="btn btn-secondary" href="${ctx}/profile">
            <i class="bi bi-x-lg"></i> Cancel
          </a>
        </div>
      </form>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
