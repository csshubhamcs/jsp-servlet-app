# STUDY — Beginner Handover & Architecture Guide

Welcome. This guide hands the **Employee Telephone Directory (JSP/Servlet
edition)** over to you. By the end you will be able to:

- **run** the app,
- **trace** any request from the browser all the way to MySQL and back,
- **find** the exact files that implement any feature, and
- **add** a new feature or **remove** an existing one by following a recipe.

It assumes only basic **Java** and basic **SQL**. No prior web-framework
experience is needed — in fact this project deliberately uses *no* framework, so
there is nothing extra to learn first.

All file paths below are relative to `jsp-servlet-app/`. Java classes live under
`src/main/java/com/company/directory/`; JSP views live under
`src/main/webapp/WEB-INF/jsp/`.

---

## Table of contents

1. [Overview — what the app does and why no framework](#1-overview)
2. [The request flow, step by step](#2-the-request-flow-step-by-step)
3. [Startup & authentication](#3-startup--authentication)
4. [The data model](#4-the-data-model)
5. [Feature-by-feature map](#5-feature-by-feature-map)
6. [How to ADD a feature](#6-how-to-add-a-feature)
7. [How to REMOVE a feature](#7-how-to-remove-a-feature)
8. [Build & run internals](#8-build--run-internals)

---

## 1. Overview

### What the app does

It is a **company phone book**.

- An **employee** logs in, fills in their contact profile once (name,
  department, phone, email, …), and can change their own password.
- An **admin** searches the directory for colleagues and approves or rejects
  "please let me edit my profile again" requests.
- A **super admin** does everything an admin does, *plus* creates accounts,
  changes roles, deactivates users, and resets passwords.

A normal user's profile **locks after the first save** — this is intentional, so
the directory stays accurate. To change it again the user raises an
*edit-request* and an admin approves it.

### Why plain Servlet / JSP / JDBC (no framework)

This project uses **no Spring, no Spring Boot, no ORM (Hibernate/JPA)** — only
the raw building blocks of Java web development:

| Piece            | What it is                                                            |
|------------------|-----------------------------------------------------------------------|
| **Servlet**      | A Java class that handles an HTTP request (`doGet` / `doPost`).       |
| **JSP**          | An HTML template with small dynamic bits, used to render a page.      |
| **JDBC**         | The raw Java database API. You write the SQL by hand.                 |
| **MySQL**        | The relational database.                                              |
| **Tomcat 10**    | The *servlet container* — the web server that actually runs the app.  |

**Why this matters for you as a beginner:** there is no "magic". No annotation
silently wires objects together; no library secretly generates SQL. You can
read a request from its first line to its last and see *everything* that
happens. The trade-off is more boilerplate — you write the SQL and you copy
result columns into objects by hand — but nothing is hidden, so it is an
excellent codebase to learn from.

The app is compiled into a single `.war` file and deployed to Tomcat. Login uses
the standard Servlet **`HttpSession`** — no JWT, no tokens.

---

## 2. The request flow, step by step

The app is **layered**. Each layer has exactly one job and only talks to the
layer directly below it. This is the single most important idea in the whole
codebase — once you see it, every file has an obvious home.

```
   Servlet  →  Service  →  DAO  →  MySQL
  (HTTP)      (rules)     (SQL)
```

### Worked example: "What happens when I click *Login*?"

Follow one real request end to end. You type `superadmin` / your password and
press **Sign in**. The browser sends `POST /login`. Here is every hop:

```
   Browser
      │  POST /login   (username + password in the form body)
      ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  AuthFilter            filter/AuthFilter.java                 │  runs FIRST,
 │  "/login" is a PUBLIC path → let it straight through.         │  on EVERY request
 └─────────────────────────────────────────────────────────────┘
      │
      ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  LoginServlet.doPost   servlet/LoginServlet.java              │  HTTP handling
 │  - reads request.getParameter("username") / ("password")      │
 │  - asks the DAO for the user, checks the password             │
 └─────────────────────────────────────────────────────────────┘
      │  userDao.findByUsername("superadmin")
      ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  AppUserDao.findByUsername   dao/AppUserDao.java              │  all the SQL
 │  - SELECT … FROM app_user WHERE username = ?                  │
 │  - copies the row into an AppUser object (mapRow)             │
 └─────────────────────────────────────────────────────────────┘
      │  JDBC connection borrowed from the HikariCP pool
      ▼
   MySQL   table: app_user
      │  one row
      ▼
 ...back in LoginServlet.doPost:
   PasswordUtil.matches(typedPassword, user.getPasswordHash())  ← BCrypt check
   request.getSession(false).invalidate()        ← drop any old session
   session = request.getSession(true)            ← brand-new session
   session.setAttribute("sessionUser", new SessionUser(user))
   response.sendRedirect("/")                    ← tell the browser to go to /
      │
      ▼
   Browser follows the redirect →  GET /
      │
      ▼
   AuthFilter (logged in now) → HomeServlet → redirect to /change-password
   (because the seeded super admin still has mustChangePassword = true)
      │
      ▼
   ChangePasswordServlet.doGet → forwards to change-password.jsp → HTML
```

> **Note:** `LoginServlet` calls the DAO **directly** (it does not go through a
> service), because logging in has no real business rule beyond "password
> matches". Most other servlets call a *service* method instead — see below.

### A request that touches every layer: `GET /profile`

```
   Browser  ──GET /profile──▶  AuthFilter  ──▶  ProfileServlet
                                                     │ userService.getById(id)
                                                     ▼
                                                UserService
                                                     │ userDao.findById(id)
                                                     ▼
                                                AppUserDao  ──SQL──▶  MySQL
                                                     ▲                  │
                                                     └────── AppUser ◀───┘
   Browser  ◀──HTML──  profile/view.jsp  ◀──forward──  ProfileServlet
```

### What each layer does (in plain words)

**1. `AuthFilter` — the gatekeeper.** `filter/AuthFilter.java`

A *filter* is code that runs **before every request reaches a servlet**. It is
mapped to `/*` with `@WebFilter("/*")`, so nothing escapes it. It does four
things, in order:

1. Lets **public paths** through untouched: `/`, `/login`, `/logout`, `/error`,
   and anything under `/static/`.
2. If there is **no logged-in user** in the session → redirect to `/login`.
3. If the logged-in user still has `mustChangePassword` set → redirect *every*
   request to `/change-password` (the only page they may visit).
4. Enforces **role rules**: `/directory` and `/requests` need Admin or above;
   `/admin/*` needs Super Admin. A violation renders the error page with HTTP
   403.

Putting all of this in one place means no servlet has to repeat the "are you
allowed here?" check.

**2. Servlets — HTTP handling only.** `servlet/*.java`

One servlet handles one area of the app and is mapped to a URL with
`@WebServlet(...)`. Its job is narrow: read request parameters and the session,
call **one** service method, then either **forward** to a JSP or send a
**redirect**. Servlets contain **no business rules and no SQL**. Example:
`ProfileServlet.doPost` reads the form into a `ProfileForm` and calls
`userService.updateOwnProfile(...)` — it does not itself decide whether the
profile is locked.

**3. Services — the business rules.** `service/*.java`

A service holds the real decisions: "is this username taken?", "is the profile
locked?", "does this user already have a pending request?". Services call DAOs
to read/write data but **never touch HTTP** (no request, no response, no
session). Because of that they can be unit-tested with mocked DAOs and *no
database* — see `src/test/`.

**4. DAOs — all the SQL.** `dao/*.java`

DAO = *Data Access Object*. Every line of JDBC and every SQL string lives here.
A DAO method borrows a connection from the pool, runs a `PreparedStatement`, and
copies the `ResultSet` rows into model objects with a `mapRow(...)` helper. All
SQL uses `?` placeholders — never string concatenation of user input — which
prevents SQL injection. If a `SQLException` happens, the DAO throws an
`AppException` with a friendly message.

**5. The model — plain data objects.** `model/*.java`

`AppUser` and `EditRequest` are plain Java objects (fields + getters/setters).
`Role` and `RequestStatus` are enums. They simply carry data between layers.

**6. JSP views — rendering HTML.** `webapp/WEB-INF/jsp/*.jsp`

A JSP is an HTML template. The servlet puts data into request attributes
(`request.setAttribute("user", user)`) and forwards to the JSP, which reads
those attributes with the **JSTL** tags (`<c:if>`, `<c:forEach>`, `<c:out>`) and
**EL** expressions (`${user.fullName}`). The JSPs live **under `WEB-INF/`**,
which means a browser *cannot* request them directly — they are reachable only
when a servlet forwards to them. Shared layout pieces (`<head>`, navbar, footer)
live in `jsp/fragments/` as `.jspf` include files.

### Why the separation pays off

Because each layer only knows the one below it, you can change one without
breaking the others:

- Swap MySQL for PostgreSQL? Only the **DAOs** change.
- Add a new business rule? It goes in a **service**; servlets and JSPs are
  untouched.
- Redesign a page? Only the **JSP** changes.

It also makes the services testable in isolation. When you add a feature, this
layering tells you *exactly* which files you will touch.

---

## 3. Startup & authentication

### Startup: how the Super Admin is seeded

`listener/BootstrapListener.java` is a **`ServletContextListener`** — code that
runs **once when the web app starts**, before any request is served. It is
registered with the `@WebListener` annotation.

Its job is *seeding*. It checks whether a Super Admin account already exists
(`AppUserDao.findByUsername`); if not, it creates one:

- Username/password come from the `APP_SUPERADMIN_USERNAME` /
  `APP_SUPERADMIN_PASSWORD` environment variables, defaulting to
  `superadmin` / `ChangeMe123!`.
- The password is **BCrypt-hashed** before it is stored.
- The new row has `mustChangePassword = true`, so the very first login forces a
  password change.

This guarantees there is always a way to log in to a fresh database.

### Login: how a session is created

When `LoginServlet.doPost` verifies the username and password it:

1. **Invalidates** any existing session (this prevents a *session-fixation*
   attack — an attacker can't pre-seed your session id).
2. Creates a **fresh** `HttpSession`.
3. Stores a small `SessionUser` object in it under the key
   `SessionUser.SESSION_KEY` (the string `"sessionUser"`).

`SessionUser` (`util/SessionUser.java`) is a lightweight snapshot — id,
username, full name, role, and the `mustChangePassword` flag. It is **not** the
full `AppUser`, so the password hash never sits in the session. Every later
request reads this object back to know who is logged in. `LogoutServlet` simply
invalidates the session.

### How `AuthFilter` guards pages

On every non-public request `AuthFilter` reads the `SessionUser` from the
session and applies the rules listed in §2. Two helper methods on `SessionUser`
make the role checks readable:

- `isAdminOrAbove()` — true for `ADMIN` and `SUPER_ADMIN`.
- `isSuperAdmin()` — true only for `SUPER_ADMIN`.

The navbar (`fragments/navbar.jspf`) *also* hides links a user may not use — but
that is only cosmetic. **The filter is the real enforcement.** If someone types
a forbidden URL directly, the filter still stops them.

---

## 4. The data model

The entire database is **two tables**, defined in `db/schema.sql`.

### `app_user` — accounts *and* directory profiles

One row per person. It deliberately mixes *authentication* fields and the
*directory profile* fields in a single table (simple, no joins needed to show a
profile).

| Column                 | In plain words                                                       |
|------------------------|----------------------------------------------------------------------|
| `id`                   | Primary key, auto-increment.                                         |
| `username`             | Login name. Must be unique.                                          |
| `password_hash`        | The BCrypt hash of the password — never the plain text password.    |
| `role`                 | `USER`, `ADMIN`, or `SUPER_ADMIN`, stored as text.                   |
| `active`               | `false` means soft-deleted — the user can't log in or be searched.   |
| `must_change_password` | `true` forces a password change before the account can be used.     |
| `profile_completed`    | `true` once the user has saved their profile at least once.          |
| `profile_locked`       | `true` after a normal user saves their profile — blocks more edits.  |
| `full_name`            | Display name (required).                                             |
| `employee_id`          | Optional HR/employee identifier.                                     |
| `department`           | Optional — e.g. "Engineering".                                       |
| `position`             | Optional — job title.                                                |
| `location`             | Optional — office/site.                                              |
| `address`              | Optional postal address.                                             |
| `work_phone`           | Optional desk phone number.                                          |
| `mobile`               | Optional mobile number.                                              |
| `email`                | Optional email address.                                              |
| `created_at`           | Set automatically when the row is inserted.                          |
| `updated_at`           | Set automatically on every update.                                   |

### `edit_request` — the profile-unlock workflow

One row per request from a locked user asking to edit their profile again.

| Column           | In plain words                                                       |
|------------------|----------------------------------------------------------------------|
| `id`             | Primary key, auto-increment.                                         |
| `requester_id`   | Foreign key → `app_user.id`. Who asked.                              |
| `reason`         | The text the user typed explaining why.                              |
| `status`         | `PENDING`, `APPROVED`, or `REJECTED`.                                |
| `created_at`     | When the request was raised.                                         |
| `resolved_by_id` | Foreign key → `app_user.id`. The admin who decided (nullable).       |
| `resolved_at`    | When it was resolved (nullable).                                     |

Two indexes speed up common lookups: `idx_edit_request_status` (finding pending
requests) and `idx_app_user_role`.

---

## 5. Feature-by-feature map

For **each** feature: a short "how it works", then a precise **Files involved**
list. This is what lets you find a feature, trace it, or remove it cleanly. The
method names below are real — you can search for them.

### 5.1 Login & logout

**How it works.** The login form posts username + password to `LoginServlet`.
It loads the user (`AppUserDao.findByUsername`), checks the account is `active`,
and verifies the password with BCrypt (`PasswordUtil.matches`). On success it
invalidates any old session, creates a fresh one, and stores a `SessionUser`.
The error message is deliberately generic ("Invalid username or password") so it
cannot be used to discover which usernames exist. `LogoutServlet` (POST only)
invalidates the session.

**Files involved:**
- `servlet/LoginServlet.java` — `/login`; GET shows the form, POST authenticates.
- `servlet/LogoutServlet.java` — `/logout`; POST ends the session.
- `filter/AuthFilter.java` — enforces "logged in" on every protected path.
- `util/SessionUser.java` — the object stored in the session.
- `service/PasswordUtil.java` — `hash`, `matches` (BCrypt).
- `dao/AppUserDao.java` — `findByUsername`.
- `webapp/WEB-INF/jsp/login.jsp` — the login form.

### 5.2 Forced first-login password change

**How it works.** Every new account (the seeded super admin, or any user an
admin creates) has `must_change_password = true`. After login, `AuthFilter` sees
this flag on the `SessionUser` and redirects **every** request to
`/change-password` until a new password is set. `ChangePasswordServlet`
validates the new password (min 8 chars, must match the confirmation), calls
`UserService.changePassword(...)` which clears the flag in the database, and
also clears it on the in-memory `SessionUser` so the redirect loop stops
immediately. `HomeServlet` also routes a flagged user straight to
`/change-password`.

**Files involved:**
- `servlet/ChangePasswordServlet.java` — `/change-password`.
- `filter/AuthFilter.java` — the forced-redirect logic.
- `servlet/HomeServlet.java` — routes a flagged user to the change screen.
- `service/UserService.java` — `changePassword`.
- `util/SessionUser.java` — holds and clears the `mustChangePassword` flag.
- `webapp/WEB-INF/jsp/change-password.jsp` — the form (the `forced` request
  attribute controls whether the navbar / cancel button are shown).

### 5.3 Role-based access control

**How it works.** Three roles in the `Role` enum: `USER`, `ADMIN`,
`SUPER_ADMIN`. `AuthFilter` checks the requested path against the role:
`/directory` and `/requests` need Admin-or-above
(`SessionUser.isAdminOrAbove()`); `/admin/*` needs Super Admin
(`SessionUser.isSuperAdmin()`). A violation produces an HTTP 403 rendered by
`error.jsp`. The navbar also hides links the user may not use — cosmetic only;
the filter is the real guard.

**Files involved:**
- `model/Role.java` — the enum.
- `filter/AuthFilter.java` — the path → role rules.
- `util/SessionUser.java` — `isAdminOrAbove()`, `isSuperAdmin()`.
- `webapp/WEB-INF/jsp/fragments/navbar.jspf` — conditionally shows links.

### 5.4 User management — create / change role / deactivate / reset password

**How it works.** Super Admin only. `AdminUserServlet` is mapped to
`/admin/users` and `/admin/users/*`, and uses the trailing path to choose an
action:
- `GET /admin/users` — lists all users (`AppUserDao.findAllOrderByFullName`).
- `GET /admin/users/new` — shows the create-user form.
- `POST /admin/users` — creates a user (`UserService.createUser`), then shows
  `credentials.jsp` with the username/password to hand over.
- `POST /admin/users/{id}/role` — `UserService.changeRole`.
- `POST /admin/users/{id}/delete` — `UserService.softDelete` (sets `active` to
  false; the row is kept, not deleted).
- `POST /admin/users/{id}/regenerate-password` — `UserService.regeneratePassword`
  makes a new random password; `credentials.jsp` shows it once.

New accounts always start with `must_change_password = true` and
`profile_locked = false`.

**Files involved:**
- `servlet/AdminUserServlet.java` — all the actions above.
- `service/UserService.java` — `createUser`, `changeRole`, `softDelete`,
  `regeneratePassword`, `getById`.
- `service/dto/CreateUserForm.java` — carries the new-account form fields.
- `service/PasswordUtil.java` — `hash`, `generateReadablePassword`.
- `dao/AppUserDao.java` — `existsByUsername`, `insert`, `update`,
  `findAllOrderByFullName`.
- `webapp/WEB-INF/jsp/admin/users.jsp`, `admin/create-user.jsp`,
  `admin/credentials.jsp`.
- `webapp/static/js/app.js` — `genPassword` (the "generate password" button).

### 5.5 The one-time profile edit lock

**How it works.** A normal user may fill in their profile **once**. When they
save (`POST /profile/edit`), `UserService.updateOwnProfile` writes the fields,
sets `profile_completed = true`, **and** sets `profile_locked = true`. After
that, `updateOwnProfile` throws an `AppException` if called again, and
`ProfileServlet` redirects away from the edit page when the profile is locked.
The Super Admin is exempt: `updateProfileAsSuperAdmin` never sets the lock and
skips the lock check, and `ProfileServlet` always lets a Super Admin open the
edit page.

**Files involved:**
- `service/UserService.java` — `updateOwnProfile` (locks),
  `updateProfileAsSuperAdmin` (never locks), `unlockProfile`, `applyProfile`.
- `servlet/ProfileServlet.java` — chooses which method to call and blocks the
  edit page when locked.
- `service/dto/ProfileForm.java` — carries the editable profile fields.
- `dao/AppUserDao.java` — `update`.
- `webapp/WEB-INF/jsp/profile/edit.jsp`, `profile/view.jsp`.

### 5.6 The edit-request flow

**How it works.** A locked user opens `/profile/request`, types a reason, and
posts it. `EditRequestService.raiseRequest` rejects it if the user already has a
`PENDING` request; otherwise it inserts a new `edit_request` row with status
`PENDING`. Admins and Super Admins see the queue at `/requests`
(`EditRequestServlet`, oldest first). Approving (`POST /requests/{id}/approve`)
calls `EditRequestService.approve`, which marks the request `APPROVED` **and**
calls `UserService.unlockProfile` so the user can edit again. Rejecting
(`POST /requests/{id}/reject`) marks it `REJECTED` and leaves the profile
locked. Resolving an already-resolved request throws an `AppException`.

**Files involved:**
- `servlet/ProfileServlet.java` — `/profile/request` GET shows the form, POST
  raises the request.
- `servlet/EditRequestServlet.java` — `/requests` queue, approve/reject.
- `service/EditRequestService.java` — `raiseRequest`, `pendingRequests`,
  `hasPendingRequest`, `approve`, `reject`.
- `service/UserService.java` — `unlockProfile`.
- `model/EditRequest.java`, `model/RequestStatus.java`.
- `dao/EditRequestDao.java` — `insert`, `findById`, `findByStatusOldestFirst`,
  `existsPendingForRequester`, `resolve`.
- `webapp/WEB-INF/jsp/profile/request.jsp`, `requests/list.jsp`.

### 5.7 Directory live search + pagination

**How it works.** `/directory` (Admin or above) renders `directory/list.jsp`,
which has a search box (`#directory-search`) and a results area
(`#directory-results`). As the user types, `app.js` waits 250 ms (a *debounce*,
so it does not fire on every keystroke) then fetches
`/directory/search?q=...&page=...`. Both `/directory` and `/directory/search`
are handled by the **same** `DirectoryServlet`; it checks `getServletPath()` —
`/directory` renders the full page, `/directory/search` renders only the
`directory/results.jsp` HTML fragment, which JavaScript drops into the results
area. `DirectoryService.search` trims and lowercases the query, clamps the page
number to 0+, and asks `AppUserDao` for a count and one page of matches (20 rows
per page — `DirectoryService.PAGE_SIZE`). The search matches `full_name`,
`department`, `work_phone`, and `mobile`, and only `active` users. The `Page`
object carries the totals the JSP needs to draw Previous / Next.

**Files involved:**
- `servlet/DirectoryServlet.java` — `/directory` and `/directory/search`.
- `service/DirectoryService.java` — search logic + `PAGE_SIZE`.
- `service/Page.java` — paging math (total pages, first/last, has next/prev).
- `dao/AppUserDao.java` — `countActiveMatching`, `searchActive`.
- `webapp/WEB-INF/jsp/directory/list.jsp` — the full page + search box.
- `webapp/WEB-INF/jsp/directory/results.jsp` — the table fragment (also
  `<%@ include %>`d by `list.jsp` for the first render).
- `webapp/static/js/app.js` — debounced fetch + pagination click handling.

### 5.8 Light / dark theme

**How it works.** A toggle button in the navbar switches a `data-theme`
attribute on the `<html>` element between `light` and `dark`. `app.js` saves the
choice in the browser's `localStorage`. A tiny inline script in
`fragments/head.jspf` reads `localStorage` and applies the saved theme **before**
the page paints, so there is no flash of the wrong theme. The CSS variables in
`app.css` define both palettes. This feature is **entirely client-side** — there
is no server code for it.

**Files involved:**
- `webapp/WEB-INF/jsp/fragments/head.jspf` — the early theme-applying script.
- `webapp/WEB-INF/jsp/fragments/navbar.jspf` — the toggle button.
- `webapp/static/js/app.js` — toggle handler + `localStorage` persistence.
- `webapp/static/css/app.css` — the light / dark CSS variables.

### 5.9 The seeded Super Admin

**How it works.** Covered in detail in §3. `BootstrapListener` runs once at
startup and creates the Super Admin if none exists. Listed here so the feature
map is complete.

**Files involved:**
- `listener/BootstrapListener.java`.
- `service/PasswordUtil.java` — `hash`.
- `dao/AppUserDao.java` — `findByUsername`, `insert`.
- `model/AppUser.java`, `model/Role.java`.

---

## 6. How to ADD a feature

The layered structure makes adding a feature predictable: you build it **from
the bottom up** — DAO, then service, then servlet, then JSP — then wire access.

### The recipe (add a new page)

1. **DAO (only if you need new data).** Add a method to a class in `dao/`. Write
   the SQL with `?` placeholders; copy result columns into a model object in a
   `mapRow` helper. Wrap `SQLException` in an `AppException`.
2. **Service (only if there is a rule).** Add a method in `service/` that holds
   the business rule and calls the DAO. Keep HTTP out of it — no request, no
   response, no session.
3. **Servlet.** Create a class in `servlet/` annotated with
   `@WebServlet("/your-path")`. In `doGet` / `doPost`: read parameters with
   `request.getParameter(...)`, read the logged-in user from the session, call
   your service, then `request.setAttribute(...)` and forward to a JSP — or send
   a `response.sendRedirect(...)`.
4. **JSP.** Add a `.jsp` file under `WEB-INF/jsp/`. Include the shared fragments
   for a consistent layout:
   ```jsp
   <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
   <body class="app-shell bg-app">
   <%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
   ...your content...
   <%@ include file="/WEB-INF/jsp/fragments/foot.jspf" %>
   ```
5. **Access.** If the page should be role-restricted, add a path rule in
   `filter/AuthFilter.java`. Add a navbar link in `fragments/navbar.jspf`
   (wrapped in a `<c:if>` if it is role-specific).

> You do **not** edit `web.xml` for routing. `@WebServlet` / `@WebFilter` /
> `@WebListener` annotations register everything automatically. `web.xml` only
> holds the UTF-8 encoding, the 30-minute session timeout, and the error pages.

### Worked example: add a read-only "Org Chart" page for admins

Goal: a new page at `/orgchart` that lists every user grouped by department.
Admin-or-above only.

1. **DAO** — `AppUserDao` already has `findAllOrderByFullName()`, which is enough
   data. *No DAO change needed.*
2. **Service** — there is no real rule (just "show all users grouped"). You can
   skip the service and let the servlet call the DAO directly, the same way
   `LoginServlet` does. *(If you wanted grouping logic, you would add an
   `OrgChartService` instead.)*
3. **Servlet** — create `servlet/OrgChartServlet.java`:
   ```java
   @WebServlet("/orgchart")
   public class OrgChartServlet extends HttpServlet {
       private final AppUserDao userDao = new AppUserDao();

       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse res)
               throws ServletException, IOException {
           req.setAttribute("users", userDao.findAllOrderByFullName());
           req.getRequestDispatcher("/WEB-INF/jsp/orgchart.jsp").forward(req, res);
       }
   }
   ```
4. **JSP** — create `webapp/WEB-INF/jsp/orgchart.jsp` using the fragment
   includes from the recipe above; loop over `${users}` with
   `<c:forEach var="u" items="${users}">`.
5. **Access** — in `AuthFilter`, add `/orgchart` to the Admin-or-above rule:
   ```java
   if ((path.startsWith("/directory") || path.startsWith("/requests")
           || path.startsWith("/orgchart")) && !user.isAdminOrAbove()) {
       forbidden(request, response);
       return;
   }
   ```
   Then add a navbar link inside the existing
   `<c:if test="${su.role == 'ADMIN' or su.role == 'SUPER_ADMIN'}">` block in
   `navbar.jspf`:
   ```jsp
   <a class="nav-link" href="${ctx}/orgchart">Org Chart</a>
   ```

Run `make start` (or `mvn package` + restart Tomcat) and visit
`http://localhost:8090/orgchart`. Done — no `web.xml` edit, no framework config.

### Adding one field to a profile (a smaller, common change)

Say you want to add a `linkedin` field:

1. Add the column to `db/schema.sql` (and to any already-running MySQL).
2. Add the field + getter/setter to `model/AppUser.java`.
3. Update `AppUserDao` in three places: the `COLUMNS` string, the `update` SQL
   and its parameter list, and `mapRow`.
4. Add the field to `service/dto/ProfileForm.java` and to
   `UserService.applyProfile`.
5. Add the `<input>` to `profile/edit.jsp`, read it in
   `ProfileServlet.readProfileForm`, and show it in `profile/view.jsp` (and
   optionally `directory/results.jsp`).

---

## 7. How to REMOVE a feature

Removing a feature means deleting its files **and** removing every reference to
them, so the app still compiles and runs. Work **top-down**: kill the entry
points (navbar link, filter rule) first, then the servlet, service, DAO, JSPs,
and finally the database table.

> Reminder: another agent may be polishing code files. The recipes below
> describe the steps; apply the code edits when the codebase is yours to change.

### Fully worked example: remove the edit-request feature

This strips the entire "ask an admin to unlock my profile" workflow.

**Step 1 — Delete these files entirely:**
- `servlet/EditRequestServlet.java`
- `service/EditRequestService.java`
- `service/EditRequestServiceTest.java` *(under `src/test/...`)*
- `dao/EditRequestDao.java`
- `model/EditRequest.java`
- `model/RequestStatus.java`
- `webapp/WEB-INF/jsp/profile/request.jsp`
- `webapp/WEB-INF/jsp/requests/list.jsp` (and the now-empty `jsp/requests/`
  folder)

**Step 2 — `servlet/ProfileServlet.java`** — remove every mention of edit
requests:
- delete the `EditRequestService` field and the two `import` lines for
  `EditRequestDao` / `EditRequestService`;
- in `doGet`, delete the `else if (sub.equals("/request"))` branch;
- in `doGet`'s profile branch, delete the
  `request.setAttribute("hasPending", ...)` line and the
  `consumeFlash(request, "flashRequested", "requested")` line;
- in `doPost`, delete the `else if (sub.equals("/request"))` branch.

**Step 3 — `filter/AuthFilter.java`** — in the Admin-or-above role rule, remove
`path.startsWith("/requests")` so the condition only checks `/directory`.

**Step 4 — `fragments/navbar.jspf`** — delete the
`<a ... href="${ctx}/requests">Edit Requests</a>` link.

**Step 5 — `webapp/WEB-INF/jsp/profile/view.jsp`** — remove the "raise an edit
request" button/banner and anything that reads the `hasPending` or `requested`
attributes (those attributes no longer exist).

**Step 6 — `db/schema.sql`** — delete the `CREATE TABLE edit_request (...)`
block and the `CREATE INDEX idx_edit_request_status ...` line. To wipe an
already-running database, run `make fresh-start` (Docker mode) or
`DROP TABLE edit_request;` against your own MySQL.

**Step 7 — decide what the lock now means.** Without edit-requests, a locked
profile can only ever be unlocked by a Super Admin editing it. That may be fine
— or you may also want to remove the one-time lock (next recipe).

After these steps the project compiles cleanly: nothing references the deleted
classes, the navbar has no dead link, the filter has no dead rule, and the
schema has no orphan table.

### Smaller example: remove the one-time profile lock

Keep the profile page, but let users edit freely:

1. In `UserService.updateOwnProfile`, delete the
   `if (user.isProfileLocked()) { throw ... }` check and the
   `user.setProfileLocked(true)` line — making it behave like
   `updateProfileAsSuperAdmin`.
2. In `ProfileServlet.doGet`, delete the
   `if (... user.isProfileLocked()) { redirect }` block in the `/edit` branch.
3. The `profile_locked` column can stay (harmless), or be dropped from
   `db/schema.sql` and from `AppUserDao` (`COLUMNS`, `insert`, `update`,
   `mapRow`) plus `model/AppUser.java`.
4. If you also removed the edit-request feature, there is nothing left that
   unlocks a profile — which is consistent, since profiles never lock now.

### Disabling a feature without deleting code

The fastest way to switch a feature off is at its entry point: remove its link
from `navbar.jspf` and add a path rule in `AuthFilter` that blocks the URL. The
classes stay in the codebase, dormant, ready to re-enable later.

---

## 8. Build & run internals

### How the WAR is built

`mvn package` compiles the Java, runs the tests (unless `-DskipTests` is
passed), and the `maven-war-plugin` packages everything into
`target/jsp-servlet-app.war` (the name comes from `<finalName>` in `pom.xml`). A
WAR is just a ZIP of: the compiled classes, the `WEB-INF/` directory, the JSPs,
and the static assets.

Dependencies marked `provided` in `pom.xml` (the Servlet and JSP APIs) are
**not** bundled — Tomcat already supplies them. Everything else (the MySQL
driver, HikariCP, jBCrypt, JSTL, SLF4J) **is** bundled inside the WAR's
`WEB-INF/lib`.

### How Docker mode wires MySQL + Tomcat

`docker-compose.yml` defines two services:

- **`mysql`** (`mysql:8.4`). The first time the container is created it runs
  every `.sql` file in `/docker-entrypoint-initdb.d/`; the compose file mounts
  `db/schema.sql` there, so the two tables are created automatically. MySQL's
  data lives in the named volume `directory-mysql-data` — which is why
  `make stop` keeps your data and `make fresh-start` (`docker compose down -v`)
  wipes it. A healthcheck pings MySQL so Tomcat waits until the database is
  actually ready.
- **`tomcat`** (`tomcat:10.1-jdk21-temurin`). It mounts the built WAR as
  `webapps/ROOT.war`, which makes the app run at the **context root** `/`. It
  `depends_on` the MySQL healthcheck. Its database connection is passed in as the
  environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`.

**Port mapping:** MySQL is exposed on host `3307` (→ container `3306`); Tomcat on
host `8090` (→ container `8080`). That is why the app is at
`http://localhost:8090`.

`make start` builds the WAR, runs `docker compose up -d --wait`, then
`docker compose restart tomcat` so Tomcat re-reads a freshly built WAR.

### How own-MySQL mode works

The app never hard-codes its database connection. `util/Database.java` reads
`DB_URL`, `DB_USER`, and `DB_PASSWORD` from environment variables, falling back
to local defaults (`jdbc:mysql://localhost:3307/directory`,
`directory` / `directory`). It builds **one** HikariCP connection pool for the
whole app; every DAO borrows a connection with `Database.getConnection()` and
returns it by closing it (try-with-resources).

In **own-MySQL mode**, `docker-compose.own-db.yml` starts only the Tomcat
container and loads those three variables from your `db.env` file
(`env_file: ./db.env`). Because the connection is just environment variables,
the app runs unchanged against any MySQL — local, remote, or cloud. From inside
the Tomcat container, `host.docker.internal` reaches a MySQL on the host
machine.

`Database.java` also sets the JDBC driver class name explicitly
(`com.mysql.cj.jdbc.Driver`) — needed because relying on `DriverManager`
auto-registration is unreliable inside a WAR's classloader — and a generous
60-second initialization timeout, because a MySQL container can be slow to
accept connections right after it starts.

### Running the tests

```bash
mvn test
```

This runs the JUnit 5 + Mockito tests under `src/test/`
(`DirectoryServiceTest`, `EditRequestServiceTest`, `UserServiceTest`,
`PasswordUtilTest`). They test the **service layer** with **mocked DAOs**, so
they need **no database** and run fast. This is possible precisely because the
services never touch HTTP or JDBC directly — the payoff of the layered design.

---

*That is the whole app. Re-read §2 (the request flow) and §5 (the feature map)
whenever you are unsure where something lives — between them they point at every
file in the codebase.*
</content>
</invoke>
