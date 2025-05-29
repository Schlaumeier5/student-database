INSERT INTO topics (name, subject, ratio, grade)
VALUES ("{0}", {1}, {2}, {3})
ON CONFLICT(name, subject, grade) DO UPDATE SET
    ratio = excluded.ratio;