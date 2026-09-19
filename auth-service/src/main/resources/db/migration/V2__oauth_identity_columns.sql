ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

ALTER TABLE users ADD COLUMN IF NOT EXISTS google_sub VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS github_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_google_sub
    ON users (google_sub) WHERE google_sub IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_github_id
    ON users (github_id) WHERE github_id IS NOT NULL;
