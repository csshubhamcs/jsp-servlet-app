<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-aurora">

<main class="app-main">
  <!-- ===================== HERO ===================== -->
  <section class="container hero">
    <h1 class="reveal d1">
      Employee<br/>
      <span class="gradient-text">Telephone Directory</span>
    </h1>
    <p class="hero-sub reveal d2">
      Find colleagues across the organisation, keep your own contact details
      up to date, and manage employee accounts and roles.
    </p>
    <div class="d-flex gap-3 justify-content-center flex-wrap reveal d3">
      <a class="btn btn-primary btn-lg" href="${ctx}/login">
        <i class="bi bi-box-arrow-in-right"></i> Login to continue
      </a>
    </div>
    <p class="mt-3 mb-0 reveal d4" style="color:var(--muted);font-size:.88rem">
      <i class="bi bi-shield-check"></i> Access is restricted to authorised employees.
    </p>
  </section>

  <!-- ===================== FEATURES ===================== -->
  <section class="container section-pad landing-features">
    <div class="row g-3">
      <div class="col-6 col-lg-3">
        <div class="feature-card sr">
          <div class="feature-icon"><i class="bi bi-search-heart"></i></div>
          <h5>Find colleagues instantly</h5>
          <p class="text-muted-2 mb-0">
            Search the directory by name and reach the right person in seconds —
            phone, mobile and email at a glance.
          </p>
        </div>
      </div>
      <div class="col-6 col-lg-3">
        <div class="feature-card sr">
          <div class="feature-icon v2"><i class="bi bi-pencil-square"></i></div>
          <h5>Keep your details current</h5>
          <p class="text-muted-2 mb-0">
            Update your own profile in a guided form. Need another change later?
            Raise a request and an admin unlocks it.
          </p>
        </div>
      </div>
      <div class="col-6 col-lg-3">
        <div class="feature-card sr">
          <div class="feature-icon v3"><i class="bi bi-diagram-3"></i></div>
          <h5>Role-based access control</h5>
          <p class="text-muted-2 mb-0">
            Users, Admins and Super Admins each see exactly what they should —
            nothing more, nothing less.
          </p>
        </div>
      </div>
      <div class="col-6 col-lg-3">
        <div class="feature-card sr">
          <div class="feature-icon v4"><i class="bi bi-shield-lock"></i></div>
          <h5>Secure by design</h5>
          <p class="text-muted-2 mb-0">
            Enforced password changes, one-time credentials and approval
            workflows keep your organisation's data protected.
          </p>
        </div>
      </div>
    </div>
  </section>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
