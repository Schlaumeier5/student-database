INSERT INTO tasks (topic, name, niveau)
VALUES (?, ?, ?)
ON CONFLICT(topic, name) DO UPDATE SET
    niveau = excluded.niveau;