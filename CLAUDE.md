# FamilyTrack - Instrucciones para Claude

## Flujo de trabajo (GitFlow)

1. Ramas principales: `main` y `develop`
2. Todos los cambios se hacen desde ramas feature creadas desde `develop`
3. Naming de ramas: `feature/#<numero>-<descripcion>`
4. Los PRs van siempre hacia `develop`
5. El merge de `develop` a `main` es responsabilidad del usuario

## Stack Técnico

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Ubicación | FusedLocationProvider |
| Notificaciones | Firebase Cloud Messaging (V1) |
| Red | Retrofit + OkHttp |
| Background | ForegroundService |
| Backend | n8n (workflows webhook) |
| Base de datos | PostgreSQL 16 |
| Infraestructura | Docker + Traefik v3 (IONOS) |

### Requisitos de compilación

- JDK 17 (Corretto/OpenJDK)
- Android SDK 34
- Gradle 8.7
- API mínima: 26 (Android 8.0+)
- Package: `com.monghit.familytrack` / `com.monghit.familytrack.debug`

## Servidor IONOS

- **SSH**: `ssh ionos1` (alias en ~/.ssh/config)
- **URL**: server.monghit.com
- **n8n UI**: https://n8n.monghit.com

### Contenedores Docker

| Container | Red | Función |
|-----------|-----|---------|
| `n8n` | traefik-net | Orquestador de workflows/webhooks |
| `familytrack-db` | traefik-net | PostgreSQL 16 |
| `traefik` | traefik-net | Reverse proxy + SSL |

### Rutas importantes en servidor

| Ruta | Contenido |
|------|-----------|
| `/opt/apps/pro/familytrack/docker-compose.yml` | Compose de PostgreSQL |
| `/opt/n8n/local-files/firebase-sa.json` | Firebase Service Account (montado en `/files/` dentro de n8n) |
| `/opt/apps/pro/n8n-backups/backup.sh` | Script de backup n8n |

## Backend - API (n8n webhooks)

**Base URL**: `https://server.monghit.com/webhook/`

Configurado en `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://server.monghit.com/webhook/\"")
```

### Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/register` | Registrar dispositivo (FCM token) |
| POST | `/api/location/update` | Enviar ubicación + validar geofences |
| GET | `/api/family/locations` | Obtener ubicaciones de la familia |
| POST | `/api/config/location-interval` | Cambiar intervalo de reporte |
| POST | `/api/notify` | Notificación manual entre familiares |
| POST | `/api/send-push` | Enviar push notification (FCM V1) |
| CRON | Cada 24h | Health check de dispositivos |

### Workflows n8n

| ID | Workflow | Endpoint | Función |
|----|----------|----------|---------|
| szQNpD8E8JA0NUq0 | Register Device | `POST /api/register` | Registra dispositivo con token FCM |
| 7AoJKxLu5XJUZmk6 | Location Update | `POST /api/location/update` | Guarda ubicación + valida geofences |
| lA6F0YjeZOO8wcBf | Family Locations | `GET /api/family/locations` | Devuelve ubicaciones familiares |
| lio5OnRuAAeyJcYe | Config Interval | `POST /api/config/location-interval` | Cambia intervalo de reporte |
| Ra2UKsJkuqo3eLsk | Manual Notify | `POST /api/notify` | Envía notificación entre familiares |
| W5xxwjdZMuPeT6xX | Health Check | CRON 24h | Detecta dispositivos offline |
| 0T5QZkWzblHs6z3N | Send Push | `POST /api/send-push` | Envía push via FCM V1 |

### Flujo Send Push (FCM V1)

```
Code node (genera JWT con crypto+fs) → HTTP Request (intercambia JWT por access_token) → HTTP Request (envía FCM) → Response
```

Requiere `NODE_FUNCTION_ALLOW_BUILTIN=crypto,fs` en el container n8n.

## Base de datos PostgreSQL

**Conexión**: `docker exec -it familytrack-db psql -U postgres -d familytrack`

### Tablas

| Tabla | Columnas principales |
|-------|---------------------|
| `users` | id, name, role, family_id |
| `devices` | id, user_id, device_token, device_name, location_interval |
| `locations` | id, device_id, latitude, longitude, accuracy, created_at |
| `safe_zones` | id, name, latitude, longitude, radius |
| `alerts` | id, type, user_id, zone_id |

### Queries útiles

```sql
-- Ubicaciones recientes
SELECT u.name, l.latitude, l.longitude, l.created_at
FROM locations l
JOIN devices d ON l.device_id = d.id
JOIN users u ON d.user_id = u.id
ORDER BY l.created_at DESC LIMIT 10;
```

## Google APIs

El proyecto usa 3 servicios de Google:

### Google Maps SDK for Android

- **Uso**: Mapa familiar con marcadores de ubicación de cada miembro (`MapScreen.kt`)
- **Librería**: `com.google.android.gms:play-services-maps:19.0.0`
- **Compose wrapper**: `com.google.maps.android:maps-compose:6.1.0`
- **API Key**: Se lee de `local.properties` (`MAPS_API_KEY`) y se inyecta via `manifestPlaceholders` en `app/build.gradle.kts`
- **Manifest**: `<meta-data android:name="com.google.android.geo.API_KEY" android:value="${MAPS_API_KEY}" />`
- **Restricción recomendada**: Apps Android con package `com.monghit.familytrack.debug` + SHA-1 del debug keystore
- **Coordenadas default**: Madrid (40.4168, -3.7038)

### Google Play Services Location

- **Uso**: Obtención de ubicación GPS del dispositivo (`LocationForegroundService.kt`)
- **Librería**: `com.google.android.gms:play-services-location:21.3.0`
- **Proveedor**: `FusedLocationProviderClient`
- **Prioridad**: `PRIORITY_HIGH_ACCURACY`
- **Intervalo default**: 5 minutos (300.000ms), configurable 1-60 min
- **Modo**: ForegroundService con tipo `location`

### Firebase (Google)

| Campo | Valor |
|-------|-------|
| Project ID | family-track-5548b |
| Sender ID | 411545008321 |
| FCM API | V1 habilitada |
| Firebase BOM | 33.1.2 |

- **FCM**: Push notifications via n8n (JWT + HTTP Request, no SDK server-side)
- **Analytics**: Firebase Analytics habilitado
- `google-services.json` debe incluir clients para ambos packages (`com.monghit.familytrack` y `com.monghit.familytrack.debug`)
- Service Account en servidor: `/opt/n8n/local-files/firebase-sa.json`

### Configuración de API Keys

Archivo `local.properties` (no versionado):
```properties
sdk.dir=/home/TU_USUARIO/Android/Sdk
MAPS_API_KEY=AIzaSy...tu_key
```

Obtener SHA-1 para restringir la API key:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

## Estructura del proyecto

```
app/src/main/java/com/monghit/familytrack/
├── FamilyTrackApp.kt              # Application + Hilt + NotificationChannels
├── MainActivity.kt                # Activity principal (Compose)
├── di/AppModule.kt                # Hilt DI (Retrofit, Repositories)
├── data/
│   ├── remote/
│   │   ├── ApiService.kt          # Endpoints Retrofit
│   │   └── dto/ApiDtos.kt         # Request/Response DTOs
│   └── repository/
│       ├── LocationRepository.kt  # Lógica de ubicación
│       └── SettingsRepository.kt  # DataStore preferences
├── domain/model/                  # User, Device, Location, SafeZone, Alert, FamilyMember
├── services/
│   ├── LocationForegroundService.kt  # Servicio de ubicación
│   ├── FamilyTrackMessagingService.kt # FCM receiver
│   └── BootReceiver.kt               # Auto-start en boot
└── ui/
    ├── navigation/                # NavRoutes + NavHost + BottomBar
    ├── screens/                   # home, map, family, settings
    └── theme/                     # Material 3 theme
```

## Herramientas Locales

### ADB (Android Debug Bridge)
- **Ruta**: `~/Android/Sdk/platform-tools/adb`
- **Alias recomendado**: `alias adb="~/Android/Sdk/platform-tools/adb"`

### Comandos ADB Comunes
```bash
# Desinstalar app
~/Android/Sdk/platform-tools/adb uninstall com.monghit.familytrack.debug

# Instalar APK
~/Android/Sdk/platform-tools/adb install ~/Git/personal/monghithub/apk_android/familytrack/app/build/outputs/apk/debug/app-debug.apk

# Ver logs (logcat)
~/Android/Sdk/platform-tools/adb logcat | grep familytrack

# Shell interactivo
~/Android/Sdk/platform-tools/adb shell
```

### Gradle Build
```bash
# Compilar y generar APK
cd ~/Git/personal/monghithub/apk_android/familytrack
./gradlew assembleDebug
# APK generado en: app/build/outputs/apk/debug/app-debug.apk
```

## Troubleshooting

### Gradle: BuildConfig cacheado tras cambiar BASE_URL
```bash
./gradlew --no-build-cache --no-configuration-cache clean assembleDebug
```

### n8n: "Version not found" al publicar workflow
Insertar entrada en `workflow_history` manualmente. Ver `docs/setup-guide.md`.

### n8n: Modificar SQLite
Siempre detener n8n antes. Usar `PRAGMA wal_checkpoint(TRUNCATE)` después.

### n8n: Reiniciar
```bash
docker restart n8n
```

### n8n: Importar workflow
```bash
docker cp workflow.json n8n:/tmp/
docker exec n8n n8n import:workflow --input=/tmp/workflow.json
```

### ADB: INSTALL_FAILED_USER_RESTRICTED (Xiaomi)
Activar "Instalar via USB" en Opciones de desarrollador.
