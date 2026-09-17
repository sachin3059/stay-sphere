CREATE TABLE IF NOT EXISTS properties (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    host_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    price_per_night NUMERIC(19, 2) NOT NULL,
    max_guests INTEGER,
    bedrooms INTEGER,
    bathrooms INTEGER,
    property_type VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS property_amenities (
    property_id VARCHAR(255) NOT NULL REFERENCES properties (id),
    amenity VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS property_images (
    property_id VARCHAR(255) NOT NULL REFERENCES properties (id),
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS availability (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL REFERENCES properties (id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_availability_property ON availability (property_id, start_date, end_date);

CREATE TABLE IF NOT EXISTS blocked_dates (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    reference_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_blocked_property ON blocked_dates (property_id, start_date, end_date);

CREATE TABLE IF NOT EXISTS pricing_rules (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL,
    base_price NUMERIC(19, 2) NOT NULL,
    weekend_multiplier NUMERIC(19, 2),
    peak_season_multiplier NUMERIC(19, 2),
    peak_season_start DATE,
    peak_season_end DATE,
    minimum_stay INTEGER,
    long_stay_discount NUMERIC(19, 2),
    long_stay_threshold_nights INTEGER,
    status VARCHAR(255)
);
