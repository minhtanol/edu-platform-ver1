CREATE TABLE student_teacher_assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  teacher_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_student_teacher_assignments_teacher
  ON student_teacher_assignments(teacher_user_id)
  WHERE deleted_at IS NULL;
