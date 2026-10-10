CREATE TABLE IF NOT EXISTS payment_outbox_events (
    id UUID PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_payment_outbox_unpublished
    ON payment_outbox_events (created_at)
    WHERE published_at IS NULL;
