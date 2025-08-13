INSERT INTO students (id, first_name, last_name, email, password, class, graduation_level)
VALUES (?, ?, ?, ?, ?, ?, ?)
ON CONFLICT(id) DO UPDATE SET
    first_name = excluded.first_name,
    last_name = excluded.last_name,
    email = excluded.email,
    password = excluded.password,
    class = excluded.class,
    graduation_level = excluded.graduation_level;
