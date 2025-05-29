INSERT INTO teacher_classes (teacher_id, class_id)
VALUES ({0}, {1})
ON CONFLICT(teacher_id, class_id) DO NOTHING;