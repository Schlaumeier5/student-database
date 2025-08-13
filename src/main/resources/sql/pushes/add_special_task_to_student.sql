INSERT INTO completed_special_tasks (student, special_task)
VALUES (?, ?)
ON CONFLICT(student, special_task) DO NOTHING;