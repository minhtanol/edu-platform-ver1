ALTER TABLE media ADD COLUMN approved_at TIMESTAMPTZ;

UPDATE media
SET approved_at = updated_at
WHERE status = 'APPROVED'
  AND approved_at IS NULL;

CREATE INDEX idx_media_approved_video_cleanup
  ON media(approved_at)
  WHERE deleted_at IS NULL AND status = 'APPROVED' AND type = 'VIDEO';
