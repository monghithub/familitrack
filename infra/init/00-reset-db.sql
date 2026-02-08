-- FamilyTrack Database Reset Script
-- Este script borra TODAS las tablas y limpia la base de datos
-- Uso: psql -U familytrack -d familytrack -f 00-reset-db.sql

BEGIN;

-- Desactivar constraints para poder borrar
ALTER TABLE IF EXISTS chat_messages DROP CONSTRAINT IF EXISTS fk_chat_messages_family;
ALTER TABLE IF EXISTS chat_messages DROP CONSTRAINT IF EXISTS fk_chat_messages_user;
ALTER TABLE IF EXISTS photos DROP CONSTRAINT IF EXISTS fk_photos_from_user;
ALTER TABLE IF EXISTS photos DROP CONSTRAINT IF EXISTS fk_photos_to_user;
ALTER TABLE IF EXISTS messages DROP CONSTRAINT IF EXISTS fk_messages_user;
ALTER TABLE IF EXISTS messages DROP CONSTRAINT IF EXISTS fk_messages_family;
ALTER TABLE IF EXISTS user_families DROP CONSTRAINT IF EXISTS fk_user_families_user;
ALTER TABLE IF EXISTS user_families DROP CONSTRAINT IF EXISTS fk_user_families_family;
ALTER TABLE IF EXISTS families DROP CONSTRAINT IF EXISTS fk_families_creator;
ALTER TABLE IF EXISTS notifications_sent DROP CONSTRAINT IF EXISTS fk_notifications_device;
ALTER TABLE IF EXISTS notifications_sent DROP CONSTRAINT IF EXISTS fk_notifications_user;
ALTER TABLE IF EXISTS device_config DROP CONSTRAINT IF EXISTS fk_device_config_device;
ALTER TABLE IF EXISTS alerts DROP CONSTRAINT IF EXISTS fk_alerts_device;
ALTER TABLE IF EXISTS alerts DROP CONSTRAINT IF EXISTS fk_alerts_user;
ALTER TABLE IF EXISTS safe_zones DROP CONSTRAINT IF EXISTS fk_safe_zones_user;
ALTER TABLE IF EXISTS safe_zones DROP CONSTRAINT IF EXISTS fk_safe_zones_creator;
ALTER TABLE IF EXISTS locations DROP CONSTRAINT IF EXISTS fk_locations_device;
ALTER TABLE IF EXISTS locations DROP CONSTRAINT IF EXISTS fk_locations_user;
ALTER TABLE IF EXISTS devices DROP CONSTRAINT IF EXISTS fk_devices_user;

-- Borrar todas las tablas en orden (respetar dependencias)
DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS photos CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS notifications_sent CASCADE;
DROP TABLE IF EXISTS device_config CASCADE;
DROP TABLE IF EXISTS alerts CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS devices CASCADE;
DROP TABLE IF EXISTS safe_zones CASCADE;
DROP TABLE IF EXISTS user_families CASCADE;
DROP TABLE IF EXISTS families CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Borrar sequences si las hay
DROP SEQUENCE IF EXISTS users_user_id_seq CASCADE;
DROP SEQUENCE IF EXISTS devices_device_id_seq CASCADE;
DROP SEQUENCE IF EXISTS locations_location_id_seq CASCADE;
DROP SEQUENCE IF EXISTS safe_zones_zone_id_seq CASCADE;
DROP SEQUENCE IF EXISTS alerts_alert_id_seq CASCADE;
DROP SEQUENCE IF EXISTS device_config_config_id_seq CASCADE;
DROP SEQUENCE IF EXISTS notifications_sent_notification_id_seq CASCADE;
DROP SEQUENCE IF EXISTS families_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_families_id_seq CASCADE;
DROP SEQUENCE IF EXISTS messages_id_seq CASCADE;
DROP SEQUENCE IF EXISTS chat_messages_id_seq CASCADE;
DROP SEQUENCE IF EXISTS photos_id_seq CASCADE;

-- Borrar índices si los hay
DROP INDEX IF EXISTS idx_devices_user CASCADE;
DROP INDEX IF EXISTS idx_devices_token CASCADE;
DROP INDEX IF EXISTS idx_devices_active CASCADE;
DROP INDEX IF EXISTS idx_locations_device_timestamp CASCADE;
DROP INDEX IF EXISTS idx_locations_user_timestamp CASCADE;
DROP INDEX IF EXISTS idx_alerts_user_created CASCADE;
DROP INDEX IF EXISTS idx_alerts_unacknowledged CASCADE;
DROP INDEX IF EXISTS idx_safe_zones_user CASCADE;
DROP INDEX IF EXISTS idx_safe_zones_active CASCADE;
DROP INDEX IF EXISTS idx_messages_family CASCADE;
DROP INDEX IF EXISTS idx_chat_messages_family CASCADE;
DROP INDEX IF EXISTS idx_photos_user CASCADE;

COMMIT;

\echo '✓ Base de datos limpiada exitosamente'
\echo '✓ Ahora ejecuta: psql -U familytrack -d familytrack -f 01-schema-v2.sql'
