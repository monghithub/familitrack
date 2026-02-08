# FamilyTrack Database Reset Scripts

Estos scripts permiten resetear completamente la base de datos de FamilyTrack y llenarla con datos de ejemplo para una experiencia de primer uso realista.

## 📋 Archivos

| Archivo | Descripción |
|---------|-------------|
| `reset-database.sh` | **Script principal** - Ejecuta el reset completo de forma interactiva |
| `00-reset-db.sql` | Borra todas las tablas y sequences |
| `01-schema-v2.sql` | Crea el esquema completo con todas las tablas v2.0 |
| `02-seed-v2.sql` | Inserta datos de ejemplo para pruebas |

## 🚀 Uso Rápido

### Opción 1: Script Interactivo (Recomendado)

```bash
cd /opt/apps/pro/familytrack/infra/init  # En servidor IONOS
# O localmente:
cd ~/Git/personal/monghithub/apk_android/familytrack/infra/init

./reset-database.sh
```

El script pedirá confirmación antes de ejecutar el reset.

### Opción 2: Manual (Si el script no funciona)

```bash
# 1. Conectar a PostgreSQL
psql -h localhost -U familytrack -d familytrack

# 2. Dentro de psql, ejecutar los scripts en orden:
\i 00-reset-db.sql
\i 01-schema-v2.sql
\i 02-seed-v2.sql

# 3. Salir
\q
```

### Opción 3: Una línea (Bash)

```bash
psql -h localhost -U familytrack -d familytrack < 00-reset-db.sql && \
psql -h localhost -U familytrack -d familytrack < 01-schema-v2.sql && \
psql -h localhost -U familytrack -d familytrack < 02-seed-v2.sql
```

## 🔧 Configuración

Si tus credenciales no son las por defecto, usa variables de entorno:

```bash
export DB_USER=tu_usuario
export DB_NAME=tu_database
export DB_HOST=tu_host
export DB_PORT=5432

./reset-database.sh
```

O pasa los parámetros directamente:

```bash
psql -h mi_host -p 5433 -U otro_usuario -d otra_db < 00-reset-db.sql
```

## 📊 Datos Incluidos

### Usuarios Creados

| ID | Nombre | Email | Rol | Dispositivo |
|----|--------|-------|-----|-------------|
| 1 | Juan García | juan@example.com | admin | Samsung A50 |
| 2 | María García | maria@example.com | monitor | iPhone 12 |
| 3 | Carlos García | carlos@example.com | monitor | Xiaomi 11 |
| 4 | Sofía García | sofia@example.com | monitored | Samsung A20 |
| 5 | Lucas García | lucas@example.com | monitored | Moto G50 |
| 6 | Rosa García | rosa@example.com | monitor | iPhone X |

### Familia

- **Nombre**: Familia García
- **Código Invitación**: FAM123
- **Miembros**: 6

### Zonas Seguras

| Nombre | Centro | Radio | Usuario |
|--------|--------|-------|---------|
| Casa Familia | 40.4168, -3.7038 | 200m | Sofía, Lucas |
| Colegio Sofía | 40.4200, -3.7010 | 150m | Sofía |
| Instituto Lucas | 40.4300, -3.6950 | 200m | Lucas |

### Datos Simulados

- ✅ 6 ubicaciones en Madrid (diferentes zonas)
- ✅ 6 mensajes de chat familiar
- ✅ 3 mensajes rápidos con ubicación
- ✅ 2 fotos compartidas
- ✅ 2 alertas de zonas seguras
- ✅ Configuración de dispositivos (idioma, modo oscuro)

## 📱 Experiencia de Usuario Después del Reset

1. **Primera apertura de app**: Mostrará Splash + Onboarding (3 páginas)
2. **Después de onboarding**: Pantalla de Family Setup
   - Opción: Crear nueva familia
   - Opción: Unirme con código (FAM123)
3. **Splash PIN/Biometría**: Si establece PIN
4. **Home Screen**: Verá ubicaciones de familia, miembros, zonas seguras
5. **Map Screen**: Marcadores de ubicación en mapa
6. **Family Screen**: Lista de 6 miembros con estados
7. **Chat**: 6 mensajes previos en chat familiar
8. **Photos**: 2 fotos ya compartidas

## ⚠️ Advertencias

- **Este script borra TODO**: No se puede recuperar datos después
- **Usar solo en desarrollo**: Nunca en base de datos de producción
- **Respaldar primero**: Si tienes datos importantes, hacer backup antes

```bash
# Backup antes de reset:
pg_dump -h localhost -U familytrack familytrack > familytrack_backup.sql
```

## 🐛 Troubleshooting

### Error: "could not connect to database"

Verifica que:
- PostgreSQL está corriendo
- Credenciales son correctas
- El host/puerto es accesible

```bash
psql -h localhost -U familytrack -d familytrack -c "SELECT 1"
```

### Error: "permission denied"

El usuario no tiene permisos para crear tablas. Conecta como superuser:

```bash
psql -h localhost -U postgres -d familytrack < 00-reset-db.sql
```

### Error: "database does not exist"

Crea la base de datos primero:

```bash
createdb -U postgres -O familytrack familytrack
```

## 📝 Personalización

Para cambiar los datos de seed, edita `02-seed-v2.sql`:

- Nombres de usuarios
- Familia
- Ubicaciones (coordenadas)
- Mensajes de chat
- Fotos (base64)

## 🔄 Ciclo de Reset

Recomendado hacer reset cada vez que:

1. Haces cambios en el schema
2. Quieres volver a empezar el onboarding
3. Necesitas pruebas limpias
4. Reseteas n8n a su estado inicial

## Próximos Pasos

Después de ejecutar el reset:

1. **Reiniciar n8n** (si está corriendo):
   ```bash
   docker restart n8n
   ```

2. **Reinstalar app en móvil**:
   ```bash
   adb uninstall com.monghit.familytrack.debug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Lanzar app**: Verá Splash → Onboarding → Family Setup

## 📞 Soporte

Si tienes problemas:
- Verifica que estés en el directorio correcto
- Comprueba permisos de archivos (.sql y .sh)
- Consulta logs de PostgreSQL: `tail -f /var/log/postgresql/postgresql.log`

