INSERT INTO teacher_subjects (teacher_id, subject_id)
VALUES ({0}, {1})
ON CONFLICT (teacher_id, subject_id) DO NOTHING;