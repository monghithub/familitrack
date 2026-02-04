-- FamilyTrack Database Schema

-- Tabla: usuarios (miembros de la familia)
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'monitored', -- 'admin', 'monitor', 'monitored'
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tabla: dispositivos registrados
CREATE TABLE devices (
    device_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    device_token VARCHAR(255) UNIQUE NOT NULL,
    device_name VARCHAR(100),
    platform VARCHAR(50) DEFAULT 'android',
    location_interval INT DEFAULT 300, -- segundos (5 min default)
    is_active BOOLEAN DEFAULT TRUE,
    last_seen TIMESTAMP,
    registered_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Tabla: ubicaciones (histórico)
CREATE TABLE locations (
    location_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(user_id),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    accuracy FLOAT,
    timestamp TIMESTAMP NOT NULL,
    received_at TIMESTAMP DEFAULT NOW()
);

-- Tabla: zonas seguras (geofences)
CREATE TABLE safe_zones (
    zone_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    center_lat DECIMAL(10, 8) NOT NULL,
    center_lng DECIMAL(11, 8) NOT NULL,
    radius_meters INT NOT NULL,
    monitored_user_id INT NOT NULL REFERENCES users(user_id),
    created_by INT REFERENCES users(user_id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tabla: alertas generadas
CREATE TABLE alerts (
    alert_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id),
    user_id INT NOT NULL REFERENCES users(user_id),
    alert_type VARCHAR(50) NOT NULL, -- 'zone_exit', 'zone_entry', 'offline', 'manual'
    message TEXT,
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tabla: configuración por dispositivo
CREATE TABLE device_config (
    config_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE UNIQUE,
    location_interval INT DEFAULT 300,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Tabla: notificaciones enviadas
CREATE TABLE notifications_sent (
    notification_id SERIAL PRIMARY KEY,
    device_id INT NOT NULL REFERENCES devices(device_id),
    from_user_id INT REFERENCES users(user_id),
    title VARCHAR(255),
    message TEXT,
    sent_at TIMESTAMP DEFAULT NOW()
);

-- Índices para performance
CREATE INDEX idx_devices_user ON devices(user_id);
CREATE INDEX idx_devices_token ON devices(device_token);
CREATE INDEX idx_devices_active ON devices(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_locations_device_timestamp ON locations(device_id, timestamp DESC);
CREATE INDEX idx_locations_user_timestamp ON locations(user_id, timestamp DESC);
CREATE INDEX idx_alerts_user_created ON alerts(user_id, created_at DESC);
CREATE INDEX idx_alerts_unacknowledged ON alerts(is_acknowledged) WHERE is_acknowledged = FALSE;
CREATE INDEX idx_safe_zones_user ON safe_zones(monitored_user_id);
CREATE INDEX idx_safe_zones_active ON safe_zones(is_active) WHERE is_active = TRUE;
