# FamilyTrack

Aplicación Android para compartir ubicación en tiempo real entre miembros de una familia.

## Funcionalidades

- **Compartir ubicación**: Envío periódico configurable (1-60 min)
- **Zonas seguras**: Geofences con alertas de entrada/salida
- **Notificaciones push**: FCM V1 via n8n (alertas de zona, manual)
- **Mapa familiar**: Visualización de ubicaciones de todos los miembros
- **Health check**: Detección de dispositivos offline (cron 24h)
- **Auto-registro**: El dispositivo se registra automáticamente al abrir la app

## Stack Técnico

| Componente | Tecnología |
|------------|------------|
| **App Android** | |
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Ubicación | FusedLocationProvider |
| Notificaciones | Firebase Cloud Messaging (V1) |
| Red | Retrofit + OkHttp |
| DB Local | Room |
| Background | ForegroundService + WorkManager |
| **Backend** | |
| Orquestador | n8n (workflows webhook) |
| Base de datos | PostgreSQL 16 |
| Infraestructura | Docker + Traefik v3 (IONOS) |

## Requisitos

- JDK 17 (Corretto/OpenJDK)
- Android SDK 34
- Cuenta Firebase con FCM V1 habilitado
- Servidor con Docker y n8n

## Configuración rapida

1. Clonar: `git clone git@github.com:monghithub/familitrack.git`
2. Agregar `google-services.json` en `app/` (con client debug)
3. Crear `local.properties` con `sdk.dir` y `MAPS_API_KEY`
4. Compilar: `./gradlew assembleDebug`
5. Instalar: `adb install -t app/build/outputs/apk/debug/app-debug.apk`

Ver guia completa en [`docs/setup-guide.md`](docs/setup-guide.md).

## Arquitectura

```
familytrack/
├── app/src/main/java/com/monghit/familytrack/
│   ├── di/                  # Hilt DI (Retrofit, Repositories)
│   ├── data/
│   │   ├── remote/          # ApiService + DTOs (Retrofit)
│   │   └── repository/      # LocationRepository, SettingsRepository
│   ├── domain/model/        # User, Device, Location, SafeZone, Alert
│   ├── services/            # LocationService, FCM Service, BootReceiver
│   └── ui/screens/          # Home, Map, Family, Settings (Compose)
├── infra/
│   ├── docker-compose.yml   # PostgreSQL (produccion)
│   ├── init/                # Schema SQL + datos seed
│   └── n8n-workflows/       # Workflows exportados (JSON)
└── docs/
    └── setup-guide.md       # Guia completa de configuracion
```

## Backend (n8n + PostgreSQL)

**URL produccion:** `https://server.monghit.com/webhook/`

| Endpoint | Descripción |
|----------|-------------|
| `POST /api/register` | Registrar dispositivo (FCM token) |
| `POST /api/location/update` | Enviar ubicación + validar geofences |
| `POST /api/config/location-interval` | Cambiar intervalo de reporte |
| `POST /api/notify` | Notificación manual entre familiares |
| `POST /api/send-push` | Enviar push notification (FCM V1) |
| CRON 24h | Health check de dispositivos |

## Licencia

Uso privado - Proyecto familiar
