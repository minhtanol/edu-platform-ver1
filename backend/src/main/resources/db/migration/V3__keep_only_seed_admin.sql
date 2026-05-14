DELETE FROM student_class_mapping
WHERE student_id IN (
  SELECT s.id FROM students s WHERE s.user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
);

DELETE FROM classes
WHERE teacher_id IN (
  SELECT t.id FROM teachers t WHERE t.user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
);

DELETE FROM students
WHERE user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM teachers
WHERE user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM evaluation_histories
WHERE evaluation_id IN (
  SELECT e.id FROM evaluations e
  WHERE e.student_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
     OR e.teacher_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
);

DELETE FROM evaluations
WHERE student_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
   OR teacher_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM media
WHERE owner_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
   OR uploaded_by_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM refresh_tokens
WHERE user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM notifications
WHERE user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM chat_conversations
WHERE user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM user_roles
WHERE user_id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

DELETE FROM users
WHERE id <> 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
