USE xjtuhub;

CREATE TABLE IF NOT EXISTS reports (
  id BIGINT NOT NULL PRIMARY KEY,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  reporter_user_id BIGINT NOT NULL,
  reason VARCHAR(64) NOT NULL,
  detail TEXT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'open',
  resolved_by BIGINT NULL,
  resolved_at DATETIME(3) NULL,
  resolution_note TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  KEY idx_reports_target_status (target_type, target_id, status),
  KEY idx_reports_reporter_time (reporter_user_id, created_at),
  KEY idx_reports_status_time (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS review_tasks (
  id BIGINT NOT NULL PRIMARY KEY,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  review_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  assigned_to BIGINT NULL,
  reviewer_user_id BIGINT NULL,
  note TEXT NULL,
  reviewed_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  KEY idx_review_target (target_type, target_id),
  KEY idx_review_status_time (status, created_at),
  KEY idx_review_assigned (assigned_to, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
