INSERT INTO subjects (name)
VALUES ("{0}")
ON CONFLICT(name) DO NOTHING;