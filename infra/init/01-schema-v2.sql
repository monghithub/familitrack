-- FamilyTrack Database Schema v2.0
-- Incluye todas las tablas para features #1-#34

BEGIN;

-- ============================================================================
-- TABLA: usuarios (miembros de la familia)
-- ============================================================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'monitored', -- 'admin', 'monitor', 'monitored'
    family_id INT,
    pin_hash VARCHAR(64), -- SHA-256 hash del PIN (4 dígitos)
    avatar_base64 LONGTEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: familias (grupos de usuarios)
-- ============================================================================
CREATE TABLE families (
    family_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    invite_code VARCHAR(6) UNIQUE NOT NULL,
    created_by INT REFERENCES users(user_id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: user_families (relación muchos-a-muchos usuario-familia)
-- ============================================================================
CREATE TABLE user_families (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    family_id INT NOT NULL REFERENCES families(family_id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'monitored', -- 'admin', 'monitor', 'monitored'
    joined_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, family_id)
);

-- ============================================================================
-- TABLA: dispositivos registrados
-- ============================================================================
CREATE TABLE devices (
    device_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    device_token VARCHAR(255) UNIQUE NOT NULL, -- FCM token
    device_name VARCHAR(100),
    platform VARCHAR(50) DEFAULT 'android',
    location_interval INT DEFAULT 300, -- segundos (5 min default)
    is_active BOOLEAN DEFAULT TRUE,
    last_seen TIMESTAMP,
    battery_level INT DEFAULT 100,
    registered_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: ubicaciones (histórico)
-- ============================================================================
CREATE TABLE locations (
    location_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(user_id),
    family_id INT REFERENCES families(family_id),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    accuracy FLOAT,
    battery_level INT,
    timestamp TIMESTAMP NOT NULL,
    received_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: zonas seguras (geofences)
-- ============================================================================
CREATE TABLE safe_zones (
    zone_id SERIAL PRIMARY KEY,
    family_id INT REFERENCES families(family_id),
    name VARCHAR(100) NOT NULL,
    center_lat DECIMAL(10, 8) NOT NULL,
    center_lng DECIMAL(11, 8) NOT NULL,
    radius_meters INT NOT NULL,
    monitored_user_id INT REFERENCES users(user_id),
    created_by INT REFERENCES users(user_id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: alertas generadas
-- ============================================================================
CREATE TABLE alerts (
    alert_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id),
    user_id INT NOT NULL REFERENCES users(user_id),
    family_id INT REFERENCES families(family_id),
    alert_type VARCHAR(50) NOT NULL, -- 'zone_exit', 'zone_entry', 'offline', 'manual', 'emergency'
    message TEXT,
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: configuración por dispositivo
-- ============================================================================
CREATE TABLE device_config (
    config_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE UNIQUE,
    location_interval INT DEFAULT 300,
    dark_mode VARCHAR(20) DEFAULT 'system', -- 'system', 'light', 'dark'
    language VARCHAR(5) DEFAULT 'es', -- 'es', 'en'
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: notificaciones enviadas (log)
-- ============================================================================
CREATE TABLE notifications_sent (
    notification_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id),
    from_user_id INT REFERENCES users(user_id),
    family_id INT REFERENCES families(family_id),
    title VARCHAR(255),
    message TEXT,
    notification_type VARCHAR(50), -- 'zone_alert', 'offline', 'manual', 'emergency', 'quick_msg'
    sent_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: mensajes rápidos (ubicación + mensaje predefinido)
-- ============================================================================
CREATE TABLE messages (
    message_id SERIAL PRIMARY KEY,
    family_id INT REFERENCES families(family_id),
    user_id INT REFERENCES users(user_id),
    message_type VARCHAR(50), -- 'home', 'busy', 'school', 'doctor', 'playing'
    message_text VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    battery_level INT,
    accuracy FLOAT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: chat familiar (mensajería grupal)
-- ============================================================================
CREATE TABLE chat_messages (
    message_id SERIAL PRIMARY KEY,
    family_id INT NOT NULL REFERENCES families(family_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_location_shared BOOLEAN DEFAULT FALSE,
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- TABLA: fotos familiares
-- ============================================================================
CREATE TABLE photos (
    photo_id SERIAL PRIMARY KEY,
    family_id INT REFERENCES families(family_id),
    from_user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    to_user_id INT REFERENCES users(user_id),
    photo_base64 LONGTEXT NOT NULL,
    caption VARCHAR(200),
    is_viewed BOOLEAN DEFAULT FALSE,
    viewed_at TIMESTAMP,
    sent_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- ÍNDICES PARA PERFORMANCE
-- ============================================================================

-- Devices
CREATE INDEX idx_devices_user ON devices(user_id);
CREATE INDEX idx_devices_token ON devices(device_token);
CREATE INDEX idx_devices_active ON devices(is_active) WHERE is_active = TRUE;

-- Locations
CREATE INDEX idx_locations_device_timestamp ON locations(device_id, timestamp DESC);
CREATE INDEX idx_locations_user_timestamp ON locations(user_id, timestamp DESC);
CREATE INDEX idx_locations_family ON locations(family_id, timestamp DESC);

-- Alerts
CREATE INDEX idx_alerts_user_created ON alerts(user_id, created_at DESC);
CREATE INDEX idx_alerts_family ON alerts(family_id, created_at DESC);
CREATE INDEX idx_alerts_unacknowledged ON alerts(is_acknowledged) WHERE is_acknowledged = FALSE;

-- Safe Zones
CREATE INDEX idx_safe_zones_user ON safe_zones(monitored_user_id);
CREATE INDEX idx_safe_zones_family ON safe_zones(family_id);
CREATE INDEX idx_safe_zones_active ON safe_zones(is_active) WHERE is_active = TRUE;

-- Messages & Chat
CREATE INDEX idx_messages_family ON messages(family_id, created_at DESC);
CREATE INDEX idx_chat_messages_family ON chat_messages(family_id, created_at DESC);
CREATE INDEX idx_chat_messages_user ON chat_messages(user_id);

-- Photos
CREATE INDEX idx_photos_from_user ON photos(from_user_id);
CREATE INDEX idx_photos_family ON photos(family_id);

-- Families
CREATE INDEX idx_families_invite_code ON families(invite_code);
CREATE INDEX idx_families_active ON families(is_active) WHERE is_active = TRUE;

-- User Families
CREATE INDEX idx_user_families_family ON user_families(family_id);
CREATE INDEX idx_user_families_user ON user_families(user_id);

COMMIT;

\echo '✓ Schema v2.0 creado exitosamente'
\echo '✓ Ahora ejecuta: psql -U familytrack -d familytrack -f 02-seed-v2.sql'
