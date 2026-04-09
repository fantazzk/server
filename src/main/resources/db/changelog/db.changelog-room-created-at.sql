ALTER TABLE rooms
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_rooms_status_created_at ON rooms (status, created_at);
