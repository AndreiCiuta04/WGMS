-- Keep existing local H2 databases compatible with the current ApplicationUser entity.
-- Hibernate creates these columns on fresh databases; these statements repair older file DBs.
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS failed_login_attempts integer NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS account_locked_until timestamp;
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS anonymized boolean NOT NULL DEFAULT false;
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS terms_accepted_at timestamp;
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS terms_version varchar(255);
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS privacy_acknowledged_at timestamp;
ALTER TABLE IF EXISTS application_user ADD COLUMN IF NOT EXISTS privacy_version varchar(255);

-- FR-10: mark shopping items that are still suggestions.
ALTER TABLE IF EXISTS shopping_item
ADD COLUMN IF NOT EXISTS suggested boolean NOT NULL DEFAULT false;