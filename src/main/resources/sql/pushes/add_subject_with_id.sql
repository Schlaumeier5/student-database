INSERT INTO subjects (id, name)
VALUES ({0}, "{1}")
ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name
ON CONFLICT(name) DO NOTHING;