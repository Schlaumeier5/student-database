INSERT INTO taskstats (student, task, status)
VALUES ({0}, {1}, {2})
ON CONFLICT(student, task) DO UPDATE SET
    status = excluded.status;