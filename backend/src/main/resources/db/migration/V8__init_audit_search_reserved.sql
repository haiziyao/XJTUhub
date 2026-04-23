CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT NOT NULL PRIMARY KEY,
  actor_user_id BIGINT NULL,
  admin_account_id BIGINT NULL,
  action VARCHAR(128) NOT NULL,
  target_type VARCHAR(64) NULL,
  target_id BIGINT NULL,
  request_id VARCHAR(64) NULL,
  ip_address VARCHAR(45) NULL,
  ip_hash VARCHAR(128) NULL,
  user_agent_hash VARCHAR(128) NULL,
  details_json JSON NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_audit_actor_time (actor_user_id, created_at),
  KEY idx_audit_target_time (target_type, target_id, created_at),
  KEY idx_audit_action_time (action, created_at),
  KEY idx_audit_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS search_index_tasks (
  id BIGINT NOT NULL PRIMARY KEY,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  operation VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  attempt_count INT NOT NULL DEFAULT 0,
  last_error VARCHAR(1000) NULL,
  next_retry_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_search_tasks_status_retry (status, next_retry_at),
  KEY idx_search_tasks_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS campus_app_login_sessions (
  id BIGINT NOT NULL PRIMARY KEY,
  scene_id VARCHAR(128) NOT NULL,
  qr_token_hash VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'pending',
  matched_user_id BIGINT NULL,
  expires_at DATETIME(3) NOT NULL,
  scanned_at DATETIME(3) NULL,
  confirmed_at DATETIME(3) NULL,
  canceled_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_campus_scene_id (scene_id),
  UNIQUE KEY uk_campus_qr_token_hash (qr_token_hash),
  KEY idx_campus_status_expiry (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
