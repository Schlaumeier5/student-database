INSERT INTO special_tasks (name, ratio, subject_id)
VALUES ("{0}", {1}, {2})
ON CONFLICT(subject_id, name) DO UPDATE SET
    ratio = excluded.ratio;