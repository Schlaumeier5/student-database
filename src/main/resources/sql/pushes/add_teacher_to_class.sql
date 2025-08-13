INSERT INTO teacher_classes (teacher_id, class_id)
VALUES (?, ?)
ON CONFLICT(teacher_id, class_id) DO NOTHING;