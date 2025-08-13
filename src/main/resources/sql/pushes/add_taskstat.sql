INSERT INTO taskstats (student, task, status)
VALUES (?, ?, ?)
ON CONFLICT(student, task) DO UPDATE SET
    status = excluded.status;