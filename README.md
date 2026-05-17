# Employee Telephone Directory — JSP/Servlet Edition

An **Employee Telephone Directory** built with plain **Java Servlets + JSP +
JDBC + MySQL** — no Spring, no framework. Employees keep their own contact
details, colleagues search the directory, and administrators manage accounts and
roles. The app is packaged as a `.war` and runs on Apache Tomcat 10. It is
deliberately simple and layered (servlet → service → DAO) so it is easy to read,
learn from, and extend.

What you can do in the app:

- **Log in** and **change your password** (the first login forces a change).
- **Fill in your contact profile** once (it then locks to keep the directory tidy).
- **Search the directory** for colleagues by name, department, or phone.
- **Raise an edit-request** to ask an admin to unlock your profile again.
- **Manage users** (admins): create accounts, set roles, deactivate, reset passwords.

> **New to the codebase?** Read [`STUDY.md`](STUDY.md) — a beginner handover and
> architecture guide that traces a real request end to end, maps every feature
> to its files, and gives step-by-step recipes for adding and removing features.

## Requirements

- **Java 21** (JDK)
- **Maven** 3.9+
- **Docker** + **Docker Compose** (used to run MySQL and/or Tomcat)

## How to run

There are two ways to run the app. **Docker mode** is the zero-setup default.
**Bring-your-own-MySQL mode** lets you point the app at any MySQL server you
already run.

### Docker mode (default — zero setup)

MySQL **and** Tomcat both run in Docker containers. You do not need a local
MySQL installation. The database schema is applied automatically the first time
the MySQL container is created.

```bash
make start        # build the WAR, start MySQL + Tomcat
make stop         # stop both containers (database volume is kept)
make fresh-start  # wipe the database volume, rebuild, start clean
```

The app is then available at **http://localhost:8090**.

What each target does:

| Target            | What it does                                                              |
|-------------------|---------------------------------------------------------------------------|
| `make start`      | `mvn package` → `docker compose up` → restarts Tomcat so it loads the WAR  |
| `make stop`       | `docker compose down` — containers stop, the MySQL data volume survives    |
| `make fresh-start`| `docker compose down -v` (drops the data volume) → rebuild → start clean   |

### Bring-your-own-MySQL mode (use any MySQL server)

Use this when you want Tomcat to run in Docker but connect to a **MySQL server
you already have** — on your laptop, on another machine, or in the cloud. The
app reads its database connection from environment variables, so any reachable
MySQL works.

**Step 1 — Prepare your MySQL server.** Create the `directory` database and a
user, then load the schema:

```sql
CREATE DATABASE IF NOT EXISTS directory
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'directory'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON directory.* TO 'directory'@'%';
FLUSH PRIVILEGES;
```

```bash
mysql -u directory -p directory < db/schema.sql
```

`db/schema.sql` creates the two tables (`app_user`, `edit_request`) and their
indexes. The two tables are the entire schema.

**Step 2 — Configure the connection.** Copy the example file and edit it:

```bash
cp db.env.example db.env
```

`db.env` holds three values:

```
DB_URL=jdbc:mysql://host.docker.internal:3306/directory
DB_USER=directory
DB_PASSWORD=your_password
```

> **Reaching a MySQL on your host machine:** Tomcat runs inside Docker, so
> `localhost` would mean *the container itself*, not your machine. Use
> `host.docker.internal` as the hostname to reach a MySQL running on the host
> (works on macOS and Windows). On Linux, use the host's LAN IP address instead.
> For a remote/cloud MySQL, just use its real hostname.

`db.env` is listed in `.gitignore`, so your credentials are never committed.

**Step 3 — Start only the Tomcat container:**

```bash
make start-own-db
```

This builds the WAR and starts Tomcat (from `docker-compose.own-db.yml`), which
reads `db.env` for its connection settings. No MySQL container is started.

The app is available at **http://localhost:8090**.

## First login

On first startup (both modes), a **Super Admin** account is seeded
automatically by `BootstrapListener` if one does not already exist:

- **Username:** `superadmin`
- **Password:** `ChangeMe123!`

You are **forced to change this password** on first login before you can use the
app. (The seed credentials can be overridden with the `APP_SUPERADMIN_USERNAME`
and `APP_SUPERADMIN_PASSWORD` environment variables.)

## Roles

Every account has exactly one of three roles. Each role can do everything the
row above it can, plus more:

| Role            | What they can do                                                              |
|-----------------|-------------------------------------------------------------------------------|
| **User**        | Edit their own profile **once** (it then locks), change their own password, and raise an edit-request to ask an admin to unlock it. |
| **Admin**       | Everything a User can, plus: browse and search the directory, and approve/reject edit-requests. |
| **Super Admin** | Everything an Admin can, plus: create users, change any user's role, deactivate users, reset passwords, and edit *any* profile (their own profile never locks). |

Access is enforced on **every request** by `AuthFilter` — see `STUDY.md` §3.

## Project layout

```
jsp-servlet-app/
├── pom.xml                       Maven build — produces jsp-servlet-app.war
├── Makefile                      start / stop / fresh-start / start-own-db
├── docker-compose.yml            Docker mode: MySQL 8 + Tomcat 10
├── docker-compose.own-db.yml     Own-MySQL mode: Tomcat 10 only
├── db.env.example                Template for own-MySQL connection settings
├── db/schema.sql                 The two table definitions + indexes
└── src/
    ├── main/
    │   ├── java/com/company/directory/
    │   │   ├── model/            AppUser, EditRequest, Role, RequestStatus
    │   │   ├── util/             Database (HikariCP pool), SessionUser
    │   │   ├── dao/              AppUserDao, EditRequestDao — all JDBC
    │   │   ├── service/          UserService, EditRequestService,
    │   │   │                     DirectoryService, PasswordUtil — business rules
    │   │   ├── servlet/          One servlet per area (login, profile, …)
    │   │   ├── filter/           AuthFilter — guards every request
    │   │   └── listener/         BootstrapListener — seeds the Super Admin
    │   └── webapp/
    │       ├── WEB-INF/
    │       │   ├── web.xml        Encoding, session timeout, error pages
    │       │   └── jsp/           All JSP views (reachable only via servlets)
    │       └── static/           app.css, app.js
    └── test/java/...             JUnit 5 + Mockito service-layer unit tests
```

## Tests

```bash
mvn test
```

Runs the service-layer unit tests (`DirectoryServiceTest`,
`EditRequestServiceTest`, `UserServiceTest`, `PasswordUtilTest`). They use
Mockito to mock the DAO layer, so **no database is required** to run them.
