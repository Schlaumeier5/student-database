INSERT INTO rooms (label, minimum_level)
VALUES ("{0}", {1})
ON CONFLICT(label) DO UPDATE SET
    label = excluded.label,
    minimum_level = excluded.minimum_level