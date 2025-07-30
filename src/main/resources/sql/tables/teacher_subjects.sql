CREATE TABLE IF NOT EXISTS teacher_subjects (
    teacher_id INTEGER,
    subject_id INTEGER,
    PRIMARY KEY (teacher_id, subject_id),
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);