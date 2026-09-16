CREATE EXTENSION IF NOT EXISTS btree_gist;

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
