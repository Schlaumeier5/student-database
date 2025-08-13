INSERT INTO classes (label, grade)
VALUES (?, ?)
ON CONFLICT(label, grade) DO NOTHING;