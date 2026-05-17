CREATE TABLE app_user (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(60)  NOT NULL UNIQUE,
    password_hash        VARCHAR(100) NOT NULL,
    role                 VARCHAR(20)  NOT NULL,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN      NOT NULL DEFAULT TRUE,
    profile_completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    profile_locked       BOOLEAN      NOT NULL DEFAULT FALSE,
    full_name            VARCHAR(120) NOT NULL,
    employee_id          VARCHAR(40),
    department           VARCHAR(80),
    position             VARCHAR(80),
    location             VARCHAR(80),
    address              VARCHAR(255),
    work_phone           VARCHAR(40),
    mobile               VARCHAR(40),
    email                VARCHAR(120),
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE edit_request (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id   BIGINT       NOT NULL,
    reason         VARCHAR(500) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_by_id BIGINT,
    resolved_at    TIMESTAMP NULL,
    CONSTRAINT fk_req_requester FOREIGN KEY (requester_id)   REFERENCES app_user(id),
    CONSTRAINT fk_req_resolver  FOREIGN KEY (resolved_by_id) REFERENCES app_user(id)
);

CREATE INDEX idx_edit_request_status ON edit_request(status);
CREATE INDEX idx_app_user_role       ON app_user(role);
