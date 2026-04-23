USE xjtuhub;

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT NOT NULL PRIMARY KEY,
  content_id BIGINT NOT NULL,
  parent_id BIGINT NULL,
  root_id BIGINT NULL,
  author_user_id BIGINT NOT NULL,
  reply_to_user_id BIGINT NULL,
  anonymous TINYINT(1) NOT NULL DEFAULT 0,
  body TEXT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'published',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  KEY idx_comments_content_time (content_id, created_at),
  KEY idx_comments_root_time (root_id, created_at),
  KEY idx_comments_author_time (author_user_id, created_at),
  KEY idx_comments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_reactions (
  id BIGINT NOT NULL PRIMARY KEY,
  content_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reaction_type VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_content_reaction (content_id, user_id, reaction_type),
  KEY idx_content_reactions_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comment_reactions (
  id BIGINT NOT NULL PRIMARY KEY,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reaction_type VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_comment_reaction (comment_id, user_id, reaction_type),
  KEY idx_comment_reactions_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
