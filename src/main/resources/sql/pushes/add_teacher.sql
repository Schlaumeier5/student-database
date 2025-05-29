INSERT INTO teachers (first_name, last_name, email, password)
VALUES ("{0}", "{1}", "{2}", "{3}")
ON CONFLICT(first_name, last_name) DO UPDATE SET
    email = excluded.email,
    password = excluded.password;