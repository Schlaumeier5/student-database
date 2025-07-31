CREATE TABLE IF NOT EXISTS teacher_classes (
    teacher_id INTEGER NOT NULL,
    class_id INTEGER NOT NULL,
    PRIMARY KEY (teacher_id, class_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);