INSERT INTO teacher_subjects (teacher_id, subject_id)
VALUES (?, ?)
ON CONFLICT (teacher_id, subject_id) DO NOTHING;