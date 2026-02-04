-- FamilyTrack - Datos iniciales de prueba

-- Insertar usuario admin
INSERT INTO users (name, role) VALUES ('Admin', 'admin');

-- Insertar miembros de ejemplo
INSERT INTO users (name, role) VALUES ('Madre', 'monitor');
INSERT INTO users (name, role) VALUES ('Padre', 'monitor');
INSERT INTO users (name, role) VALUES ('Hijo', 'monitored');
INSERT INTO users (name, role) VALUES ('Abuela', 'monitored');

-- Zona segura de ejemplo: Casa (Madrid centro)
INSERT INTO safe_zones (name, center_lat, center_lng, radius_meters, monitored_user_id, created_by)
VALUES ('Casa', 40.41680000, -3.70380000, 200, 4, 1);

-- Zona segura de ejemplo: Colegio
INSERT INTO safe_zones (name, center_lat, center_lng, radius_meters, monitored_user_id, created_by)
VALUES ('Colegio', 40.42000000, -3.70100000, 150, 4, 1);
