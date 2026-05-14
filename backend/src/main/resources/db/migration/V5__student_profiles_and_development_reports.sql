CREATE TABLE student_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  address VARCHAR(1000),
  guardian_name VARCHAR(255),
  guardian_phone VARCHAR(50),
  hometown VARCHAR(255),
  allergies VARCHAR(1000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

CREATE TABLE student_development_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  week_start DATE NOT NULL,
  physical_change VARCHAR(2000),
  cognitive_change VARCHAR(2000),
  social_change VARCHAR(2000),
  emotional_change VARCHAR(2000),
  note VARCHAR(4000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ,
  UNIQUE(student_id, teacher_id, week_start)
);

CREATE INDEX idx_student_development_reports_student_week
  ON student_development_reports(student_id, week_start DESC)
  WHERE deleted_at IS NULL;
