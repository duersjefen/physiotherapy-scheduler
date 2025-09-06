-- Create appointments table for physiotherapy scheduling
CREATE TABLE IF NOT EXISTS appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    start_time TEXT NOT NULL,              -- ISO8601 UTC timestamp
    duration_minutes INTEGER NOT NULL,      -- Duration in minutes
    client_name TEXT,                      -- Client name (nullable)
    client_email TEXT,                     -- Client email (nullable)
    status TEXT NOT NULL DEFAULT 'available', -- Status: 'available', 'booked', 'cancelled'
    created_at TEXT NOT NULL,              -- ISO8601 UTC timestamp
    updated_at TEXT NOT NULL               -- ISO8601 UTC timestamp
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_appointments_start_time ON appointments(start_time);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_appointments_start_status ON appointments(start_time, status);
