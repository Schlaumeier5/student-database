INSERT INTO classtopics (grade, topic)
VALUES (?, ?)
ON CONFLICT(grade, topic) DO NOTHING;