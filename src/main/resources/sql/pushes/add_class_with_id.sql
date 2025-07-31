INSERT INTO classes (id, label, grade)
VALUES ({0}, "{1}", {2})
ON CONFLICT (id) DO UPDATE SET label = EXCLUDED.label, grade = EXCLUDED.grade
ON CONFLICT(label, grade) DO NOTHING;