CREATE TABLE IF NOT EXISTS teacher_classes (
    teacher_id INTEGER NOT NULL,
    class_id INTEGER NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    FOREIGN KEY (class_id) REFERENCES classes(id)
);