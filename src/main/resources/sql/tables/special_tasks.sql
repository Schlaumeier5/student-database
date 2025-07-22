CREATE TABLE IF NOT EXISTS special_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    ratio REAL NOT NULL,
    subject_id INTEGER,

    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    UNIQUE (subject_id, name)
);