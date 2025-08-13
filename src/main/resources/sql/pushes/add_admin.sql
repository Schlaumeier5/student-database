INSERT INTO admins (username, password_hash)
VALUES (?, ?)
ON CONFLICT (username) DO
UPDATE SET password_hash = EXCLUDED.password_hash;