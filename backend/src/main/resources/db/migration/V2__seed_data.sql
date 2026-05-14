INSERT INTO roles (id, name) VALUES
  ('11111111-1111-1111-1111-111111111111', 'ADMIN'),
  ('22222222-2222-2222-2222-222222222222', 'TEACHER'),
  ('33333333-3333-3333-3333-333333333333', 'STUDENT');

INSERT INTO permissions (name, description) VALUES
  ('USER_MANAGE', 'Manage users and roles'),
  ('MEDIA_APPROVE', 'Approve media submissions'),
  ('EVALUATION_WRITE', 'Create and update evaluations'),
  ('AI_CHAT', 'Use AI assistant');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.name = 'ADMIN';
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.name IN ('EVALUATION_WRITE','AI_CHAT') WHERE r.name = 'TEACHER';
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.name = 'AI_CHAT' WHERE r.name = 'STUDENT';

INSERT INTO users (id, email, password_hash, full_name) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin@education.com', 'CHANGE_ON_START_Admin@123', 'System Admin'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'teacher@education.com', 'CHANGE_ON_START_Teacher@123', 'Demo Teacher'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'student@education.com', 'CHANGE_ON_START_Student@123', 'Demo Student');

INSERT INTO user_roles (user_id, role_id) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333');

INSERT INTO teachers (id, user_id, employee_code) VALUES ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'T-0001');
INSERT INTO students (id, user_id, student_code, guardian_email) VALUES ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'S-0001', 'guardian@example.com');
INSERT INTO classes (id, name, teacher_id) VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Demo Class', 'dddddddd-dddd-dddd-dddd-dddddddddddd');
INSERT INTO student_class_mapping (student_id, class_id) VALUES ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'ffffffff-ffff-ffff-ffff-ffffffffffff');
