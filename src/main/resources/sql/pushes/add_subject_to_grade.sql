INSERT INTO gradesubjects
VALUES (?, ?)
ON CONFLICT(grade, subject) DO NOTHING;