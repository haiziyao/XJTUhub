USE xjtuhub;

CREATE OR REPLACE VIEW v_file_download_logs_public AS
SELECT
  id,
  attachment_id,
  content_id,
  user_id,
  ip_hash,
  user_agent_hash,
  review_status_at_download,
  created_at
FROM file_download_logs;

CREATE OR REPLACE VIEW v_file_download_logs_security AS
SELECT
  id,
  attachment_id,
  content_id,
  user_id,
  ip_address,
  ip_hash,
  user_agent_hash,
  review_status_at_download,
  created_at
FROM file_download_logs;

CREATE OR REPLACE VIEW v_audit_logs_public AS
SELECT
  id,
  actor_user_id,
  admin_account_id,
  action,
  target_type,
  target_id,
  request_id,
  ip_hash,
  user_agent_hash,
  details_json,
  created_at
FROM audit_logs;

CREATE OR REPLACE VIEW v_audit_logs_security AS
SELECT
  id,
  actor_user_id,
  admin_account_id,
  action,
  target_type,
  target_id,
  request_id,
  ip_address,
  ip_hash,
  user_agent_hash,
  details_json,
  created_at
FROM audit_logs;
