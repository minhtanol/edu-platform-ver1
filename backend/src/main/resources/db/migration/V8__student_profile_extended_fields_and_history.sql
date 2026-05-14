ALTER TABLE student_profiles
  ADD COLUMN date_of_birth DATE,
  ADD COLUMN gender VARCHAR(20),
  ADD COLUMN emergency_contact_name VARCHAR(255),
  ADD COLUMN emergency_contact_phone VARCHAR(50),
  ADD COLUMN study_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

CREATE TABLE student_profile_histories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  before_value VARCHAR(8000),
  after_value VARCHAR(8000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_student_profile_histories_student
  ON student_profile_histories(student_user_id, created_at DESC)
  WHERE deleted_at IS NULL;
