CREATE TABLE IF NOT EXISTS classes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    label TEXT NOT NULL,
    grade INTEGER NOT NULL,
    UNIQUE(label, grade)
)