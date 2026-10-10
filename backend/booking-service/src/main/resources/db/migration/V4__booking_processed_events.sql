CREATE TABLE IF NOT EXISTS booking_processed_events (
    event_id VARCHAR(512) PRIMARY KEY,
    consumer_name VARCHAR(128) NOT NULL
);
