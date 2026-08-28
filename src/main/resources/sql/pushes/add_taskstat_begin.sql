INSERT INTO taskstats (student, task, status, attempts)
VALUES (?, ?, 1, 1)
ON CONFLICT(student, task) DO UPDATE SET
    status = 1,
    attempts = taskstats.attempts + 1;
