-- Optional MySQL permission template.
-- Review and replace usernames, hosts, and passwords before running.
-- This file is not safe to run unchanged in production.

USE xjtuhub;

-- Example accounts. Replace passwords before use.
-- CREATE USER IF NOT EXISTS 'xjtuhub_app'@'%' IDENTIFIED BY 'CHANGE_ME_APP_PASSWORD';
-- CREATE USER IF NOT EXISTS 'xjtuhub_security'@'%' IDENTIFIED BY 'CHANGE_ME_SECURITY_PASSWORD';

-- Normal application account:
-- - can use normal tables as required by backend
-- - should prefer public views for download/audit inspection
-- - should not be granted direct plaintext-IP diagnostic access casually
--
-- Adjust write privileges after backend module permissions are finalized.
-- GRANT SELECT, INSERT, UPDATE, DELETE ON xjtuhub.* TO 'xjtuhub_app'@'%';
-- REVOKE SELECT ON xjtuhub.v_file_download_logs_security FROM 'xjtuhub_app'@'%';
-- REVOKE SELECT ON xjtuhub.v_audit_logs_security FROM 'xjtuhub_app'@'%';
-- GRANT SELECT ON xjtuhub.v_file_download_logs_public TO 'xjtuhub_app'@'%';
-- GRANT SELECT ON xjtuhub.v_audit_logs_public TO 'xjtuhub_app'@'%';

-- Security audit account:
-- - can read security views that include plaintext IP
-- - should not be used by ordinary application runtime paths
--
-- GRANT SELECT ON xjtuhub.v_file_download_logs_security TO 'xjtuhub_security'@'%';
-- GRANT SELECT ON xjtuhub.v_audit_logs_security TO 'xjtuhub_security'@'%';

-- FLUSH PRIVILEGES;
