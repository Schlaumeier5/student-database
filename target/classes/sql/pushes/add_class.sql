INSERT INTO classes (label, grade)
VALUES ("{0}", {1})
ON CONFLICT(label, grade) DO NOTHING;