INSERT INTO school_years (label, week_count, current_week)
VALUES ("{0}", {1}, {2})
ON CONFLICT(label) DO UPDATE SET
    week_count = EXCLUDED.week_count,
    current_week = EXCLUDED.current_week;