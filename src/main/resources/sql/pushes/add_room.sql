INSERT INTO rooms (label, minimum_level)
VALUES (?, ?)
ON CONFLICT(label) DO UPDATE SET
    label = excluded.label,
    minimum_level = excluded.minimum_level