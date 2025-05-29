INSERT INTO classtopics (grade, topic)
VALUES ({0}, {1})
ON CONFLICT(grade, topic) DO NOTHING;