INSERT INTO tasks (topic, name, niveau)
VALUES ({0}, "{1}", {2})
ON CONFLICT(topic, name) DO UPDATE SET
    niveau = excluded.niveau;