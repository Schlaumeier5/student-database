INSERT INTO topics (name, subject, ratio, grade, number)
VALUES ("{0}", {1}, {2}, {3}, {4})
ON CONFLICT(name, subject, grade) DO UPDATE SET
    ratio = excluded.ratio;