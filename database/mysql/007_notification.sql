USE xjtuhub;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL PRIMARY KEY,
  target_user_id BIGINT NOT NULL,
  actor_user_id BIGINT NULL,
  type VARCHAR(64) NOT NULL,
  title VARCHAR(200) NOT NULL,
  body VARCHAR(1000) NULL,
  related_content_id BIGINT NULL,
  related_comment_id BIGINT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'unread',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  KEY idx_notifications_user_status_time (target_user_id, status, created_at),
  KEY idx_notifications_type_time (type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_deliveries (
  id BIGINT NOT NULL PRIMARY KEY,
  notification_id BIGINT NOT NULL,
  channel VARCHAR(32) NOT NULL,
  delivery_status VARCHAR(32) NOT NULL DEFAULT 'pending',
  provider_message_id VARCHAR(255) NULL,
  error_message VARCHAR(1000) NULL,
  sent_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_delivery_notification (notification_id),
  KEY idx_delivery_status_time (delivery_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
