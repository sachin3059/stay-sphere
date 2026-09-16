CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE properties
    ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(description, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(city, '')), 'C')
    ) STORED;

CREATE INDEX IF NOT EXISTS idx_properties_search_vector
    ON properties USING GIN (search_vector);

CREATE INDEX IF NOT EXISTS idx_properties_title_trgm
    ON properties USING GIN (title gin_trgm_ops);
