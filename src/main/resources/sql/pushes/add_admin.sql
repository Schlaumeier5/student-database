INSERT INTO admins (username, password_hash)
VALUES ("{0}", "{1}")
ON CONFLICT (username) DO
UPDATE SET password_hash = EXCLUDED.password_hash;