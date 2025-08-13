INSERT INTO teachers (first_name, last_name, email, password)
VALUES (?, ?, ?, ?)
ON CONFLICT(first_name, last_name) DO UPDATE SET
    email = excluded.email,
    password = excluded.password;