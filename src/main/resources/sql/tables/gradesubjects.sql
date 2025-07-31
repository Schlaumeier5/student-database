CREATE TABLE IF NOT EXISTS gradesubjects (
    grade INTEGER NOT NULL,
    subject INTEGER NOT NULL,
    
    UNIQUE(grade, subject),

    FOREIGN KEY (subject) REFERENCES subjects(id) ON DELETE CASCADE
)