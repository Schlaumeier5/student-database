CREATE TABLE IF NOT EXISTS topics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    subject INTEGER NOT NULL,
    ratio INTEGER NOT NULL,
    grade INTEGER NOT NULL,
    resource TEXT,
    UNIQUE(name, subject, grade)
)