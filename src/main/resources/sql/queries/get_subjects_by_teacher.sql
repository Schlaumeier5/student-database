SELECT s.*
FROM subjects s
JOIN teacher_subjects ts ON s.id = ts.subject_id
WHERE ts.teacher_id = ?;