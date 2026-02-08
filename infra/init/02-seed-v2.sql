-- FamilyTrack Seed Data v2.0
-- Datos de ejemplo para primera experiencia de usuario

BEGIN;

-- ============================================================================
-- INSERTAR FAMILIA
-- ============================================================================
INSERT INTO families (name, invite_code, created_by, is_active)
VALUES ('Familia García', 'FAM123', NULL, TRUE);

-- ============================================================================
-- INSERTAR USUARIOS
-- ============================================================================
-- Admin (creador de familia)
INSERT INTO users (name, email, role, family_id)
VALUES ('Juan García', 'juan@example.com', 'admin', 1);

-- Monitor (padres)
INSERT INTO users (name, email, role, family_id)
VALUES ('María García', 'maria@example.com', 'monitor', 1);

INSERT INTO users (name, email, role, family_id)
VALUES ('Carlos García', 'carlos@example.com', 'monitor', 1);

-- Monitoreados (hijos)
INSERT INTO users (name, email, role, family_id)
VALUES ('Sofía García', 'sofia@example.com', 'monitored', 1);

INSERT INTO users (name, email, role, family_id)
VALUES ('Lucas García', 'lucas@example.com', 'monitored', 1);

-- Abuela
INSERT INTO users (name, email, role, family_id)
VALUES ('Rosa García', 'rosa@example.com', 'monitor', 1);

-- ============================================================================
-- RELACIONES USER-FAMILY
-- ============================================================================
INSERT INTO user_families (user_id, family_id, role)
VALUES 
    (1, 1, 'admin'),     -- Juan - admin
    (2, 1, 'monitor'),   -- María - monitor
    (3, 1, 'monitor'),   -- Carlos - monitor
    (4, 1, 'monitored'), -- Sofía - monitored
    (5, 1, 'monitored'), -- Lucas - monitored
    (6, 1, 'monitor');   -- Rosa - monitor

-- ============================================================================
-- INSERTAR DISPOSITIVOS
-- ============================================================================
-- Dispositivos de adultos
INSERT INTO devices (user_id, device_token, device_name, location_interval, battery_level)
VALUES 
    (1, 'fcm_token_juan_001', 'Samsung A50 Juan', 300, 85),
    (2, 'fcm_token_maria_001', 'iPhone 12 María', 300, 92),
    (3, 'fcm_token_carlos_001', 'Xiaomi 11 Carlos', 300, 78);

-- Dispositivos de niños (con polling más frecuente)
INSERT INTO devices (user_id, device_token, device_name, location_interval, battery_level)
VALUES 
    (4, 'fcm_token_sofia_001', 'Samsung A20 Sofía', 180, 65),
    (5, 'fcm_token_lucas_001', 'Moto G50 Lucas', 180, 45),
    (6, 'fcm_token_rosa_001', 'iPhone X Rosa', 300, 88);

-- ============================================================================
-- INSERTAR UBICACIONES INICIALES (Madrid)
-- ============================================================================
-- Ubicaciones de hoy (simuladas en diferentes zonas)
INSERT INTO locations (device_id, user_id, family_id, latitude, longitude, accuracy, battery_level, timestamp)
VALUES 
    -- Juan: En la oficina
    (1, 1, 1, 40.4530, -3.6883, 12.5, 85, NOW() - INTERVAL '5 minutes'),
    
    -- María: En casa
    (2, 2, 1, 40.4168, -3.7038, 8.3, 92, NOW() - INTERVAL '3 minutes'),
    
    -- Carlos: En el parque
    (3, 3, 1, 40.4437, -3.6891, 15.2, 78, NOW() - INTERVAL '2 minutes'),
    
    -- Sofía: En el colegio
    (4, 4, 1, 40.4200, -3.7010, 10.5, 65, NOW() - INTERVAL '1 minute'),
    
    -- Lucas: Camino a casa
    (5, 5, 1, 40.4250, -3.6950, 18.7, 45, NOW()),
    
    -- Rosa: En casa
    (6, 6, 1, 40.4168, -3.7038, 7.2, 88, NOW() - INTERVAL '4 minutes');

-- ============================================================================
-- INSERTAR ZONAS SEGURAS
-- ============================================================================
INSERT INTO safe_zones (family_id, name, center_lat, center_lng, radius_meters, monitored_user_id, created_by, is_active)
VALUES 
    -- Para Sofía (colegio y casa)
    (1, 'Colegio Sofía', 40.4200, -3.7010, 150, 4, 1, TRUE),
    (1, 'Casa Familia', 40.4168, -3.7038, 200, 4, 1, TRUE),
    
    -- Para Lucas
    (1, 'Instituto Lucas', 40.4300, -3.6950, 200, 5, 1, TRUE),
    (1, 'Casa Familia', 40.4168, -3.7038, 200, 5, 1, TRUE);

-- ============================================================================
-- INSERTAR MENSAJES RÁPIDOS DE EJEMPLO
-- ============================================================================
INSERT INTO messages (family_id, user_id, message_type, message_text, latitude, longitude, battery_level, created_at)
VALUES 
    (1, 2, 'home', 'Voy a casa', 40.4168, -3.7038, 92, NOW() - INTERVAL '30 minutes'),
    (1, 4, 'school', 'En el colegio', 40.4200, -3.7010, 65, NOW() - INTERVAL '15 minutes'),
    (1, 5, 'busy', 'Ocupado', 40.4250, -3.6950, 45, NOW() - INTERVAL '5 minutes');

-- ============================================================================
-- INSERTAR CHAT FAMILIAR
-- ============================================================================
INSERT INTO chat_messages (family_id, user_id, message, is_location_shared, created_at)
VALUES 
    (1, 1, '¡Hola a todos! ¿Cómo están?', FALSE, NOW() - INTERVAL '30 minutes'),
    (1, 2, 'Hola amor, aquí en casa. Sofía llegó del colegio bien.', FALSE, NOW() - INTERVAL '25 minutes'),
    (1, 4, 'Papá, ¿a qué hora vienes?', FALSE, NOW() - INTERVAL '20 minutes'),
    (1, 1, 'En 30 minutos salgo de la oficina', FALSE, NOW() - INTERVAL '18 minutes'),
    (1, 5, 'Lucas está en camino a casa', FALSE, NOW() - INTERVAL '10 minutes'),
    (1, 6, 'Abuela aquí, preparando merienda para los niños 😊', FALSE, NOW() - INTERVAL '5 minutes');

-- ============================================================================
-- INSERTAR FOTOS
-- ============================================================================
INSERT INTO photos (family_id, from_user_id, to_user_id, photo_base64, caption, is_viewed, sent_at)
VALUES 
    (1, 4, NULL, 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==', 
     '¡Mira lo que hicimos en el arte hoy!', TRUE, NOW() - INTERVAL '2 hours'),
     
    (1, 5, NULL, 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
     'Pastel en el recreo con amigos 🎂', TRUE, NOW() - INTERVAL '1 hour');

-- ============================================================================
-- INSERTAR ALERTAS DE EJEMPLO
-- ============================================================================
INSERT INTO alerts (device_id, user_id, family_id, alert_type, message, location_lat, location_lng, is_acknowledged, created_at)
VALUES 
    (4, 4, 1, 'zone_entry', 'Sofía entró a la zona: Colegio Sofía', 40.4200, -3.7010, TRUE, NOW() - INTERVAL '8 hours'),
    (5, 5, 1, 'zone_exit', 'Lucas salió de la zona: Instituto Lucas', 40.4300, -3.6950, FALSE, NOW() - INTERVAL '2 hours');

-- ============================================================================
-- INSERTAR CONFIGURACIÓN DE DISPOSITIVOS
-- ============================================================================
INSERT INTO device_config (device_id, location_interval, dark_mode, language)
VALUES 
    (1, 300, 'system', 'es'),
    (2, 300, 'light', 'es'),
    (3, 300, 'system', 'es'),
    (4, 180, 'dark', 'es'),
    (5, 180, 'system', 'es'),
    (6, 300, 'light', 'es');

COMMIT;

\echo '✓ Datos de seed insertados exitosamente'
\echo '✓ Base de datos lista para usar'
\echo ''
\echo 'USUARIOS CREADOS:'
\echo '  1. Juan García (admin) - juan@example.com'
\echo '  2. María García (monitor) - maria@example.com'
\echo '  3. Carlos García (monitor) - carlos@example.com'
\echo '  4. Sofía García (monitored) - sofia@example.com'
\echo '  5. Lucas García (monitored) - lucas@example.com'
\echo '  6. Rosa García (monitor) - rosa@example.com'
\echo ''
\echo 'FAMILIA:'
\echo '  Nombre: Familia García'
\echo '  Código Invitación: FAM123'
\echo ''
\echo 'ZONAS SEGURAS:'
\echo '  - Casa Familia (40.4168, -3.7038) - 200m'
\echo '  - Colegio Sofía (40.4200, -3.7010) - 150m'
\echo '  - Instituto Lucas (40.4300, -3.6950) - 200m'
