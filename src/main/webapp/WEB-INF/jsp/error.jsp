<%--
  error.jsp — generic error page shown for 403, 404, 500, and uncaught exceptions.

  The standard servlet error attributes are set by the container:
    jakarta.servlet.error.status_code — HTTP status code (Integer), if applicable
    jakarta.servlet.error.exception   — the Throwable, if applicable

  The ErrorServlet (mapped in web.xml) pre-processes these and sets 'message'
  to a safe, user-friendly string. AuthFilter also forwards here for 403 Forbidden,
  setting 'message' directly.

  The navbar is shown only if the user has an active session.
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="statusCode" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<body class="app-shell bg-aurora">

<c:if test="${not empty sessionScope.sessionUser}">
  <%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
</c:if>

<main class="app-main d-flex align-items-center" style="padding:6vh 0;">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-12 col-sm-10 col-md-7 col-lg-6">
        <div class="auth-card text-center reveal">
          <div class="auth-logo mx-auto"
               style="background:linear-gradient(135deg,#d4493a,#c0392b);box-shadow:0 10px 24px rgba(192,57,43,.34)">
            <i class="bi bi-shield-exclamation"></i>
          </div>
          <span class="page-eyebrow" style="color:var(--danger);background:var(--danger-bg)">
            <c:choose>
              <c:when test="${not empty statusCode}"><c:out value="${statusCode}"/></c:when>
              <c:otherwise>Error</c:otherwise>
            </c:choose>
          </span>
          <h3 class="mb-2">
            <c:choose>
              <c:when test="${statusCode == 403}">You don't have access to this page</c:when>
              <c:when test="${statusCode == 404}">Page not found</c:when>
              <c:otherwise>Something went wrong</c:otherwise>
            </c:choose>
          </h3>
          <p class="text-muted-2 mb-4">
            <c:choose>
              <c:when test="${statusCode == 403}">Your account doesn't have permission to view this page.
                If you think this is a mistake, contact your administrator.</c:when>
              <c:when test="${statusCode == 404}">The page you were looking for doesn't exist or has moved.</c:when>
              <c:otherwise>
                <c:choose>
                  <c:when test="${not empty message}"><c:out value="${message}"/></c:when>
                  <c:otherwise>An unexpected error occurred. Please try again, or contact
                    your administrator if the problem continues.</c:otherwise>
                </c:choose>
              </c:otherwise>
            </c:choose>
          </p>
          <div class="d-flex gap-2 justify-content-center flex-wrap">
            <a class="btn btn-primary" href="${ctx}/">
              <i class="bi bi-house"></i> Back to home
            </a>
            <c:if test="${not empty sessionScope.sessionUser}">
              <a class="btn btn-secondary" href="${ctx}/profile">
                <i class="bi bi-person-circle"></i> My profile
              </a>
            </c:if>
          </div>
        </div>
      </div>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
