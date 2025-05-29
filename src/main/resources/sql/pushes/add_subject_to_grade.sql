INSERT INTO gradesubjects
VALUES ({0}, {1})
ON CONFLICT(grade, subject) DO NOTHING;