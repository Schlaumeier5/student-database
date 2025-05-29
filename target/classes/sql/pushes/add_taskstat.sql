INSERT INTO taskstats (student, task, status)
VALUES ({1}, {2}, {3})
ON CONFLICT(student, task) DO UPDATE SET
    status = excluded.status;