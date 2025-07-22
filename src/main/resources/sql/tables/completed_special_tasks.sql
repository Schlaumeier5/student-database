CREATE TABLE IF NOT EXISTS completed_special_tasks (
    student INTEGER NOT NULL,
    special_task INTEGER NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student, special_task)
)