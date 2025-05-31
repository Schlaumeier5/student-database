INSERT INTO student_topics (student_id, topic_id, subject_id)
VALUES (
    {0},
    {1},
    (SELECT subject FROM topics WHERE id = {1})
)
ON CONFLICT(student_id, subject_id) DO UPDATE SET
    topic_id = EXCLUDED.topic_id