// Employee Telephone Directory — light interactivity
(function () {
  "use strict";

  // Scroll-reveal: fade/slide elements in as they enter the viewport
  document.addEventListener("DOMContentLoaded", function () {
    var targets = document.querySelectorAll(".sr");
    if (!targets.length) return;

    if (!("IntersectionObserver" in window)) {
      targets.forEach(function (el) { el.classList.add("in"); });
      return;
    }

    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add("in");
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12 });

    targets.forEach(function (el) { io.observe(el); });
  });

  // Mark the current nav link as active
  document.addEventListener("DOMContentLoaded", function () {
    var path = window.location.pathname;
    document.querySelectorAll(".app-navbar .nav-link").forEach(function (link) {
      var href = link.getAttribute("href");
      if (!href || href === "/") return;
      if (path === href || path.indexOf(href + "/") === 0) {
        link.classList.add("active");
      }
    });
  });

  // ----- Responsive navbar hamburger toggle (no Bootstrap JS dependency) -----
  document.addEventListener("DOMContentLoaded", function () {
    var toggler = document.getElementById("nav-toggler");
    var collapse = document.getElementById("app-nav-collapse");
    if (!toggler || !collapse) return;

    function setOpen(open) {
      collapse.classList.toggle("nav-open", open);
      toggler.setAttribute("aria-expanded", open ? "true" : "false");
    }

    toggler.addEventListener("click", function () {
      setOpen(!collapse.classList.contains("nav-open"));
    });

    // Close the menu when a navigation link is tapped on mobile
    collapse.addEventListener("click", function (ev) {
      if (ev.target.closest("a.nav-link")) setOpen(false);
    });

    // Collapse the open mobile menu if the viewport grows to desktop width
    window.addEventListener("resize", function () {
      if (window.innerWidth >= 992) setOpen(false);
    });
  });

  // ----- Dark mode toggle -----
  function syncThemeIcon() {
    var isDark = document.documentElement.getAttribute("data-theme") === "dark";
    document.querySelectorAll("[data-theme-icon]").forEach(function (icon) {
      icon.className = "bi " + (isDark ? "bi-sun" : "bi-moon-stars");
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    syncThemeIcon();
    var btn = document.getElementById("theme-toggle");
    if (!btn) return;
    btn.addEventListener("click", function () {
      var isDark = document.documentElement.getAttribute("data-theme") === "dark";
      var next = isDark ? "light" : "dark";
      document.documentElement.setAttribute("data-theme", next);
      try { localStorage.setItem("theme", next); } catch (e) {}
      syncThemeIcon();
    });
  });

  // ----- Directory live search + pagination -----
  document.addEventListener("DOMContentLoaded", function () {
    var input = document.getElementById("directory-search");
    var results = document.getElementById("directory-results");
    if (!input || !results) return;

    function load(query, page) {
      fetch("/directory/search?q=" + encodeURIComponent(query) + "&page=" + page, {
        headers: { "X-Requested-With": "XMLHttpRequest" }
      })
        .then(function (r) { return r.text(); })
        .then(function (html) { results.innerHTML = html; })
        .catch(function () {});
    }

    var timer;
    input.addEventListener("input", function () {
      clearTimeout(timer);
      var value = input.value.trim();
      timer = setTimeout(function () { load(value, 0); }, 250);
    });

    // Delegated pagination clicks on the stable wrapper
    results.addEventListener("click", function (ev) {
      var target = ev.target.closest("[data-page]");
      if (!target || !results.contains(target)) return;
      ev.preventDefault();
      var page = target.getAttribute("data-page");
      if (page === null || page === "") return;
      load(input.value.trim(), page);
    });
  });
})();

// Cryptographically-strong password generator (exact charset per spec)
function genPassword(targetId) {
  var charset = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%&*?+-";
  var len = 14;
  var out = "";
  var rnd = new Uint32Array(len);
  window.crypto.getRandomValues(rnd);
  for (var i = 0; i < len; i++) {
    out += charset[rnd[i] % charset.length];
  }
  var field = document.getElementById(targetId || "password");
  if (field) {
    field.value = out;
    field.dispatchEvent(new Event("input", { bubbles: true }));
  }
  return out;
}

// ----- Password show/hide toggle -----
// Flips an input's type between password and text and swaps the eye icon.
function togglePassword(inputId, btn) {
  var field = document.getElementById(inputId);
  if (!field) return;
  var icon = btn ? btn.querySelector("i") : null;
  var reveal = field.type === "password";
  field.type = reveal ? "text" : "password";
  if (icon) {
    icon.className = "bi " + (reveal ? "bi-eye-slash" : "bi-eye");
  }
  if (btn) {
    btn.setAttribute("aria-label", reveal ? "Hide password" : "Show password");
    btn.setAttribute("title", reveal ? "Hide password" : "Show password");
  }
}

// Wire up any [data-toggle-password] buttons declaratively
document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll("[data-toggle-password]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      togglePassword(btn.getAttribute("data-toggle-password"), btn);
    });
  });
});

// ----- Confirm-password live match check (change-password page) -----
document.addEventListener("DOMContentLoaded", function () {
  var newPw = document.getElementById("newPassword");
  var confirmPw = document.getElementById("confirmPassword");
  var feedback = document.getElementById("pwMatchFeedback");
  if (!newPw || !confirmPw || !feedback) return;

  var submitBtn = document.getElementById("changePwSubmit");

  function evaluate() {
    var a = newPw.value;
    var b = confirmPw.value;
    var valid = false;

    if (a.length === 0 && b.length === 0) {
      feedback.textContent = "";
      feedback.className = "pw-match-msg";
    } else if (b.length === 0) {
      feedback.textContent = "";
      feedback.className = "pw-match-msg";
    } else if (a === b) {
      feedback.innerHTML = '<i class="bi bi-check-circle-fill"></i> Passwords match';
      feedback.className = "pw-match-msg is-ok";
      valid = a.length >= 8;
    } else {
      feedback.innerHTML = '<i class="bi bi-exclamation-circle-fill"></i> Passwords do not match';
      feedback.className = "pw-match-msg is-bad";
    }

    if (submitBtn) {
      submitBtn.disabled = !valid;
    }
  }

  newPw.addEventListener("input", evaluate);
  confirmPw.addEventListener("input", evaluate);
  evaluate();
});

// Copy text to clipboard with button feedback
function copyText(text, btn) {
  function done() {
    if (!btn) return;
    var original = btn.innerHTML;
    // Remember which colour class this button actually had so we restore it
    var colorClasses = ["btn-outline-primary", "btn-primary", "btn-outline-secondary",
                        "btn-secondary", "btn-outline-success"];
    var originalColor = colorClasses.filter(function (c) {
      return btn.classList.contains(c);
    });
    btn.innerHTML = '<i class="bi bi-check2"></i> Copied';
    originalColor.forEach(function (c) { btn.classList.remove(c); });
    btn.classList.add("btn-success");
    setTimeout(function () {
      btn.innerHTML = original;
      btn.classList.remove("btn-success");
      originalColor.forEach(function (c) { btn.classList.add(c); });
    }, 1800);
  }
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(done, function () {});
  } else {
    var ta = document.createElement("textarea");
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand("copy"); } catch (e) {}
    document.body.removeChild(ta);
    done();
  }
}
