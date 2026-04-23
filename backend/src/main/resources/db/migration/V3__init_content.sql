CREATE TABLE IF NOT EXISTS boards (
  id BIGINT NOT NULL PRIMARY KEY,
  parent_id BIGINT NULL,
  slug VARCHAR(128) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512) NULL,
  visibility VARCHAR(32) NOT NULL DEFAULT 'public',
  post_policy VARCHAR(32) NOT NULL DEFAULT 'open_publish',
  allow_anonymous TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  UNIQUE KEY uk_boards_slug (slug),
  KEY idx_boards_parent_sort (parent_id, sort_order),
  KEY idx_boards_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  slug VARCHAR(64) NOT NULL,
  category VARCHAR(64) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  UNIQUE KEY uk_tags_slug (slug),
  KEY idx_tags_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contents (
  id BIGINT NOT NULL PRIMARY KEY,
  type VARCHAR(32) NOT NULL,
  board_id BIGINT NOT NULL,
  author_user_id BIGINT NOT NULL,
  author_display_type VARCHAR(32) NOT NULL DEFAULT 'user',
  organization_id BIGINT NULL,
  anonymous TINYINT(1) NOT NULL DEFAULT 0,
  title VARCHAR(200) NOT NULL,
  body MEDIUMTEXT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  visibility VARCHAR(32) NOT NULL DEFAULT 'public',
  pinned TINYINT(1) NOT NULL DEFAULT 0,
  view_count BIGINT NOT NULL DEFAULT 0,
  published_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  KEY idx_contents_board_status_time (board_id, status, published_at),
  KEY idx_contents_author_time (author_user_id, created_at),
  KEY idx_contents_org_time (organization_id, created_at),
  KEY idx_contents_type_status_time (type, status, published_at),
  KEY idx_contents_visibility_status (visibility, status),
  KEY idx_contents_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resource_details (
  id BIGINT NOT NULL PRIMARY KEY,
  content_id BIGINT NOT NULL,
  course_code VARCHAR(64) NULL,
  course_name VARCHAR(128) NULL,
  teacher_name VARCHAR(128) NULL,
  semester VARCHAR(64) NULL,
  resource_type VARCHAR(64) NULL,
  review_status VARCHAR(32) NOT NULL DEFAULT 'unreviewed',
  download_policy VARCHAR(32) NOT NULL DEFAULT 'allow',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  UNIQUE KEY uk_resource_details_content (content_id),
  KEY idx_resource_course (course_code, course_name),
  KEY idx_resource_review_status (review_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_details (
  id BIGINT NOT NULL PRIMARY KEY,
  content_id BIGINT NOT NULL,
  starts_at DATETIME(3) NULL,
  ends_at DATETIME(3) NULL,
  location VARCHAR(255) NULL,
  registration_url VARCHAR(512) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  UNIQUE KEY uk_activity_details_content (content_id),
  KEY idx_activity_starts_at (starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_metadata (
  id BIGINT NOT NULL PRIMARY KEY,
  content_id BIGINT NOT NULL,
  meta_key VARCHAR(128) NOT NULL,
  meta_value TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  UNIQUE KEY uk_content_meta_key (content_id, meta_key),
  KEY idx_content_meta_key (meta_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_tags (
  content_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  created_by BIGINT NULL,
  PRIMARY KEY (content_id, tag_id),
  KEY idx_content_tags_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
