<%--
  requests/list.jsp — admin view of pending profile-unlock requests.

  Each row has Approve and Reject buttons. Both submit a POST form to
  /requests/{id}/approve or /requests/{id}/reject (handled by EditRequestServlet).
  Reject has a JS confirm() to prevent accidental clicks.

  Expected request attribute:
    requests — List<EditRequest> (all PENDING requests, oldest first)
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4">

    <div class="page-head d-flex flex-wrap justify-content-between align-items-end gap-3 reveal">
      <div>
        <span class="page-eyebrow"><i class="bi bi-inbox"></i> Approval queue</span>
        <h3>Pending Edit Requests</h3>
        <p class="lead-sub">Review and act on profile-unlock requests from employees.</p>
      </div>
      <span class="status-pill ${empty requests ? 'status-yes' : 'status-lock'}">
        <i class="bi bi-hourglass-split"></i>
        ${fn:length(requests)}&nbsp;pending
      </span>
    </div>

    <c:choose>
      <c:when test="${not empty requests}">
        <!-- Table -->
        <div class="table-card reveal d1">
          <div class="table-responsive">
            <table class="table align-middle">
              <thead>
                <tr>
                  <th>Requester</th>
                  <th>Reason</th>
                  <th>Submitted</th>
                  <th class="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="r" items="${requests}">
                  <tr>
                    <td>
                      <div class="name-cell">
                        <span class="avatar-chip">
                          <c:choose>
                            <c:when test="${not empty r.requesterName}">${fn:toUpperCase(fn:substring(r.requesterName, 0, 1))}</c:when>
                            <c:otherwise>?</c:otherwise>
                          </c:choose>
                        </span>
                        <span class="cell-strong"><c:out value="${r.requesterName}"/></span>
                      </div>
                    </td>
                    <td style="max-width:420px"><c:out value="${r.reason}"/></td>
                    <td>
                      <span style="white-space:nowrap">
                        <i class="bi bi-clock-history text-muted-2"></i>
                        <c:out value="${r.createdAt}"/>
                      </span>
                    </td>
                    <td>
                      <div class="d-flex gap-2 justify-content-end flex-wrap">
                        <form class="d-inline"
                              action="${ctx}/requests/${r.id}/approve" method="post">
                          <button class="btn btn-sm btn-success" type="submit">
                            <i class="bi bi-check-lg"></i> Approve
                          </button>
                        </form>
                        <form class="d-inline"
                              action="${ctx}/requests/${r.id}/reject" method="post">
                          <button class="btn btn-sm btn-outline-danger" type="submit"
                                  onclick="return confirm('Reject this edit request?');">
                            <i class="bi bi-x-lg"></i> Reject
                          </button>
                        </form>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <!-- Empty state -->
        <div class="table-card reveal d1">
          <div class="empty-state">
            <div class="empty-icon"><i class="bi bi-inbox"></i></div>
            <h5>All caught up</h5>
            <p>There are no pending edit requests right now.</p>
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
