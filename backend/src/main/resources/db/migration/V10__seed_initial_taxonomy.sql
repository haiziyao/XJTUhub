-- Fixed bootstrap IDs are deliberately small and reserved for seed taxonomy.
-- Runtime Snowflake IDs should not use this range.

INSERT INTO boards (
  id, parent_id, slug, name, description, visibility, post_policy,
  allow_anonymous, sort_order, status, created_at, updated_at
) VALUES
  (1001, NULL, 'discussion', 'Discussion', 'General campus discussion.', 'public', 'open_publish', 1, 10, 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (1002, NULL, 'resources', 'Resources', 'Course materials, files, and useful references.', 'public', 'open_publish', 0, 20, 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (1003, NULL, 'experience', 'Experience', 'Senior experience, study paths, career and graduate school sharing.', 'public', 'open_publish', 1, 30, 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (1004, NULL, 'activities-competitions', 'Activities And Competitions', 'Activities, competitions, and team-up information.', 'public', 'open_publish', 0, 40, 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (1005, NULL, 'tools-projects', 'Tools And Projects', 'Campus tools, student projects, and technical sharing.', 'public', 'open_publish', 0, 50, 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (1006, NULL, 'site-affairs', 'Site Affairs', 'Announcements, feedback, and platform governance.', 'public', 'review_required', 0, 60, 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  visibility = VALUES(visibility),
  post_policy = VALUES(post_policy),
  allow_anonymous = VALUES(allow_anonymous),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = UTC_TIMESTAMP(3);

INSERT INTO tags (
  id, name, slug, category, status, created_at, updated_at
) VALUES
  (2001, 'Course', 'course', 'general', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2002, 'College', 'college', 'general', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2003, 'Graduate Recommendation', 'graduate-recommendation', 'experience', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2004, 'Employment', 'employment', 'experience', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2005, 'Competition', 'competition', 'activity', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2006, 'Club', 'club', 'organization', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2007, 'Software', 'software', 'tool', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2008, 'Tool', 'tool', 'tool', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
  (2009, 'Freshman', 'freshman', 'experience', 'active', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  status = VALUES(status),
  updated_at = UTC_TIMESTAMP(3);
