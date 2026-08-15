ALTER TABLE taskstats ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0;
UPDATE taskstats SET attempts = 1 WHERE attempts = 0;
