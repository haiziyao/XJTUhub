USE xjtuhub;

CREATE TABLE IF NOT EXISTS attachments (
  id BIGINT NOT NULL PRIMARY KEY,
  content_id BIGINT NOT NULL,
  uploader_user_id BIGINT NOT NULL,
  storage_provider VARCHAR(32) NOT NULL,
  bucket VARCHAR(128) NOT NULL,
  object_key VARCHAR(512) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  mime_type VARCHAR(128) NULL,
  size_bytes BIGINT NOT NULL,
  checksum VARCHAR(128) NULL,
  visibility VARCHAR(32) NOT NULL DEFAULT 'inherit',
  review_status VARCHAR(32) NOT NULL DEFAULT 'unreviewed',
  file_status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  KEY idx_attachments_content (content_id),
  KEY idx_attachments_uploader_time (uploader_user_id, created_at),
  KEY idx_attachments_review_status (review_status),
  KEY idx_attachments_object (storage_provider, bucket, object_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_download_logs (
  id BIGINT NOT NULL PRIMARY KEY,
  attachment_id BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  user_id BIGINT NULL,
  ip_address VARCHAR(45) NULL,
  ip_hash VARCHAR(128) NULL,
  user_agent_hash VARCHAR(128) NULL,
  review_status_at_download VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_download_attachment_time (attachment_id, created_at),
  KEY idx_download_content_time (content_id, created_at),
  KEY idx_download_user_time (user_id, created_at),
  KEY idx_download_ip_hash_time (ip_hash, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
