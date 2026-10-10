ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP;

-- Existing accounts stay usable without forcing re-verification.
UPDATE users
SET email_verified_at = COALESCE(created_at, NOW())
WHERE email_verified_at IS NULL;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_email_verification_token_hash
    ON email_verification_tokens (token_hash);

CREATE INDEX IF NOT EXISTS idx_email_verification_user_id
    ON email_verification_tokens (user_id);
