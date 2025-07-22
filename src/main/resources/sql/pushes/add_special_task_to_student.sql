INSERT INTO completed_special_tasks (student, special_task)
VALUES ({0}, {1})
ON CONFLICT(student, special_task) DO NOTHING;