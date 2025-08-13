INSERT INTO special_tasks (name, ratio, subject_id)
VALUES (?, ?, ?)
ON CONFLICT(subject_id, name) DO UPDATE SET
    ratio = excluded.ratio;