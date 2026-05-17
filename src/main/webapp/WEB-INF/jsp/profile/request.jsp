<%--
  request.jsp — form for submitting a profile-unlock edit request.

  Users whose profile is locked can submit a reason here; an admin will review
  and approve or reject the request via the /requests queue.

  Note: this page inlines the footer rather than using foot.jspf because it
  appends a small inline character-counter script for the reason textarea.
  The foot.jspf fragment closes </body> and </html>, which would prevent appending
  the script after it. Moving the counter script above the close tags would work
  too, but the current approach is explicit and self-contained.
--%>
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="app-shell bg-app">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>

<main class="app-main">
  <div class="container py-4" style="max-width:640px;">

    <div class="page-head reveal">
      <span class="page-eyebrow"><i class="bi bi-unlock"></i> Approval workflow</span>
      <h3>Request profile edit</h3>
      <p class="lead-sub">Ask an administrator to unlock your profile for changes.</p>
    </div>

    <div class="alert alert-info mb-3 reveal d1">
      <i class="bi bi-info-circle-fill"></i>
      <span>
        Your profile is currently locked. Tell us why you need to make changes —
        an Admin or Super Admin will review and unlock it.
      </span>
    </div>

    <div class="panel p-4 reveal d2">
      <form action="${ctx}/profile/request" method="post">
        <div class="mb-2">
          <label class="form-label" for="reason">Reason for the change</label>
          <textarea class="form-control" id="reason" name="reason" rows="4"
                    required maxlength="500"
                    placeholder="e.g. My department changed after a recent transfer and my work phone is now different."></textarea>
          <div class="d-flex justify-content-end mt-1">
            <span style="color:var(--muted);font-size:.8rem">
              <span id="reasonCount">0</span> / 500 characters
            </span>
          </div>
        </div>
        <hr class="divider-soft"/>
        <div class="d-flex gap-2 flex-wrap">
          <button class="btn btn-warning" type="submit">
            <i class="bi bi-send"></i> Submit request
          </button>
          <a class="btn btn-secondary" href="${ctx}/profile">
            <i class="bi bi-x-lg"></i> Cancel
          </a>
        </div>
      </form>
    </div>
  </div>
</main>

<footer class="app-footer">
  <div class="container d-flex flex-wrap justify-content-between gap-2">
    <span><span class="brand-mark sm"><i class="bi bi-telephone-fill"></i></span>
      Employee Telephone Directory</span>
    <span class="text-muted-2">Role-based contact management</span>
  </div>
</footer>
<script src="${pageContext.request.contextPath}/static/js/app.js"></script>
<script>
  (function () {
    var ta = document.getElementById("reason");
    var counter = document.getElementById("reasonCount");
    if (ta && counter) {
      var update = function () { counter.textContent = ta.value.length; };
      ta.addEventListener("input", update);
      update();
    }
  })();
</script>
</body>
</html>
