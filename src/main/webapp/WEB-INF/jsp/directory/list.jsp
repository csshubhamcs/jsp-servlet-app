<%--
  directory/list.jsp — full employee directory page (Admin and Super Admin only).

  Renders the search box and embeds results.jsp as the initial results.
  When the user types in the search box, app.js fetches GET /directory/search
  and swaps the #directory-results div with the new results fragment — no full reload.

  Expected request attributes:
    result — Page<AppUser> (initial result set, usually the first page of everyone)
    query  — current search text (empty string on first load)
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4">

    <div class="page-head d-flex flex-wrap justify-content-between align-items-end gap-3 reveal">
      <div>
        <span class="page-eyebrow"><i class="bi bi-grid-3x3-gap"></i> Company directory</span>
        <h3>Employee Directory</h3>
        <p class="lead-sub">Search and reach colleagues across the organisation.</p>
      </div>
    </div>

    <!-- Search — filters instantly as you type, no buttons -->
    <div class="card-surface p-3 mb-3 reveal d1">
      <div class="input-affix">
        <i class="bi bi-search ic"></i>
        <input class="form-control" type="text" id="directory-search" name="q"
               value="${fn:escapeXml(query)}" autocomplete="off"
               placeholder="Search by name… results update as you type"/>
      </div>
    </div>

    <!-- Stable wrapper — never itself replaced -->
    <div id="directory-results">
      <%@ include file="/WEB-INF/jsp/directory/results.jsp" %>
    </div>

  </div>
</main>

<%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
