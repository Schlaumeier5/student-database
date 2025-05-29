SELECT subjects.*
FROM subjects, gradesubjects
WHERE
    subjects.id = gradesubjects.subject
AND gradesubjects.grade = {0}