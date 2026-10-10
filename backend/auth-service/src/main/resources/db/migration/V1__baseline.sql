-- Auth schema (Flyway runs before JPA; tables must exist here for Docker / validate profile).

CREATE TABLE IF NOT EXISTS users (
    id                  VARCHAR(36) PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255),
    full_name           VARCHAR(255) NOT NULL,
    role                VARCHAR(32) NOT NULL,
    google_sub          VARCHAR(255),
    github_id           VARCHAR(255),
    email_verified_at   TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_google_sub
    ON users (google_sub) WHERE google_sub IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_github_id
    ON users (github_id) WHERE github_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          VARCHAR(36) PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     VARCHAR(36) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at  TIMESTAMP,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens (user_id);
