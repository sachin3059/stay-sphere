CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Recover when Flyway baseline skipped empty V1 (baseline-version was 1).
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL,
    guest_id VARCHAR(255) NOT NULL,
    host_id VARCHAR(255) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_guests INTEGER NOT NULL,
    total_price NUMERIC(19, 2) NOT NULL,
    price_per_night NUMERIC(19, 2) NOT NULL,
    status VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255),
    property_lock_key VARCHAR(255),
    property_lock_token VARCHAR(255),
    pending_expires_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_bookings_idempotency_key ON bookings (idempotency_key);
CREATE INDEX IF NOT EXISTS idx_booking_guest ON bookings (guest_id);
CREATE INDEX IF NOT EXISTS idx_booking_property ON bookings (property_id);

CREATE TABLE IF NOT EXISTS waitlist_entries (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL,
    guest_id VARCHAR(255) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_guests INTEGER NOT NULL,
    queue_position INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    slot_offered_at TIMESTAMP,
    slot_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_waitlist_property ON waitlist_entries (property_id, check_in, check_out);
CREATE INDEX IF NOT EXISTS idx_waitlist_guest ON waitlist_entries (guest_id);

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS stay daterange
        GENERATED ALWAYS AS (daterange(check_in, check_out, '[)')) STORED;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'bookings_no_overlap'
    ) THEN
        ALTER TABLE bookings
            ADD CONSTRAINT bookings_no_overlap
            EXCLUDE USING gist (property_id WITH =, stay WITH &&)
            WHERE (status IN ('PENDING', 'CONFIRMED'));
    END IF;
END $$;
