# Arquitectura de FamilyTrack

## Visión General

FamilyTrack es una aplicación de geolocalización familiar compuesta por una **app Android nativa** y un **backend serverless** basado en n8n + PostgreSQL, desplegado en Docker sobre un servidor IONOS.

---

## Diagrama de Arquitectura General

```mermaid
graph TB
    subgraph "Dispositivos Android"
        APP1[App FamilyTrack<br/>Admin]
        APP2[App FamilyTrack<br/>Madre]
        APP3[App FamilyTrack<br/>Hijo]
    end

    subgraph "Google Cloud"
        FCM[Firebase Cloud Messaging<br/>V1 API]
        MAPS[Google Maps SDK]
        FLOC[FusedLocationProvider]
    end

    subgraph "Servidor IONOS"
        direction TB
        TRAEFIK[Traefik v3<br/>Reverse Proxy + SSL]

        subgraph "Docker Network: traefik-net"
            N8N[n8n<br/>Workflow Engine]
            PG[PostgreSQL 16<br/>familytrack-db]
        end

        CRON[Cron Job<br/>Backup 3:00 AM]
    end

    APP1 & APP2 & APP3 -->|HTTPS| TRAEFIK
    TRAEFIK -->|Webhook| N8N
    N8N -->|SQL| PG
    N8N -->|FCM V1 HTTP| FCM
    FCM -->|Push| APP1 & APP2 & APP3
    APP1 & APP2 & APP3 -->|Location| FLOC
    APP1 & APP2 & APP3 -->|Render Map| MAPS
    CRON -->|backup.sh| N8N
```

---

## Arquitectura de la App Android

```mermaid
graph TB
    subgraph "UI Layer (Jetpack Compose)"
        HS[HomeScreen]
        MS[MapScreen]
        FS[FamilyScreen]
        SS[SettingsScreen]
        SZS[SafeZonesScreen]
        NAV[FamilyTrackNavHost<br/>Bottom Navigation]
    end

    subgraph "ViewModel Layer"
        HVM[HomeViewModel]
        MVM[MapViewModel]
        FVM[FamilyViewModel]
        SVM[SettingsViewModel]
        SZVM[SafeZonesViewModel]
    end

    subgraph "Data Layer"
        LR[LocationRepository]
        SR[SettingsRepository<br/>DataStore]
        AS[ApiService<br/>Retrofit]
    end

    subgraph "Services"
        LFS[LocationForegroundService<br/>FusedLocationProvider]
        FCMS[FamilyTrackMessagingService<br/>FCM Receiver]
        BR[BootReceiver]
    end

    subgraph "Domain"
        USER[User]
        DEV[Device]
        LOC[Location]
        SZ[SafeZone]
        ALT[Alert]
        FM[FamilyMember]
    end

    NAV --> HS & MS & FS & SS
    MS --> SZS
    HS --> HVM
    MS --> MVM
    FS --> FVM
    SS --> SVM
    SZS --> SZVM

    HVM & MVM & FVM & SVM & SZVM --> LR
    HVM & SVM --> SR
    LR --> AS
    LFS --> LR
    LFS --> SR
    FCMS -.->|Notificaciones| NAV
    BR -->|Boot| LFS
```

---

## Arquitectura del Backend (n8n + PostgreSQL)

```mermaid
graph LR
    subgraph "Endpoints Webhook"
        W1[POST /api/register]
        W2[POST /api/location/update]
        W3[GET /api/family/locations]
        W4[POST /api/config/location-interval]
        W5[POST /api/notify]
        W6[POST /api/send-push]
        W7[POST /api/safe-zones/create]
        W8[POST /api/safe-zones/delete]
        W9[CRON 24h Health Check]
    end

    subgraph "n8n Workflows"
        WF1[Register Device]
        WF2[Location Update]
        WF3[Family Locations]
        WF4[Config Interval]
        WF5[Manual Notify]
        WF6[Send Push FCM V1]
        WF7[Create Safe Zone]
        WF8[Delete Safe Zone]
        WF9[Health Check]
    end

    subgraph "PostgreSQL"
        T1[(users)]
        T2[(devices)]
        T3[(locations)]
        T4[(safe_zones)]
        T5[(alerts)]
        T6[(device_config)]
        T7[(notifications_sent)]
    end

    subgraph "Firebase"
        FCM[FCM V1 API]
    end

    W1 --> WF1 --> T1 & T2
    W2 --> WF2 --> T3 & T4 & T5
    W3 --> WF3 --> T1 & T2 & T3 & T4
    W4 --> WF4 --> T2
    W5 --> WF5 --> T7
    W6 --> WF6 --> FCM
    W7 --> WF7 --> T4
    W8 --> WF8 --> T4
    W9 --> WF9 --> T2 & T5

    WF2 -->|Geofence alert| WF6
    WF5 -->|Send push| WF6
    WF9 -->|Offline alert| WF6
```

---

## Diagrama de Secuencia: Registro de Dispositivo

```mermaid
sequenceDiagram
    participant App as App Android
    participant FCM as Firebase FCM
    participant N8N as n8n (Register)
    participant DB as PostgreSQL

    App->>FCM: Solicitar FCM Token
    FCM-->>App: Token FCM
    App->>App: Guardar token en DataStore
    App->>N8N: POST /api/register<br/>{deviceToken, userId, deviceName}
    N8N->>DB: INSERT/UPDATE devices
    DB-->>N8N: deviceId, locationInterval
    N8N-->>App: {status, deviceId, locationInterval}
    App->>App: Guardar deviceId en DataStore
    App->>App: Iniciar LocationForegroundService
```

---

## Diagrama de Secuencia: Envío de Ubicación

```mermaid
sequenceDiagram
    participant GPS as FusedLocationProvider
    participant SVC as LocationForegroundService
    participant REPO as LocationRepository
    participant N8N as n8n (Location Update)
    participant DB as PostgreSQL
    participant FCM as Firebase FCM
    participant FAM as App Familiar

    loop Cada N minutos (configurable)
        GPS->>SVC: Nueva ubicación (lat, lng, accuracy)
        SVC->>REPO: sendLocation(location)
        REPO->>N8N: POST /api/location/update<br/>{userId, lat, lng, accuracy}
        N8N->>DB: INSERT INTO locations
        N8N->>DB: SELECT safe_zones WHERE is_active
        N8N->>N8N: Calcular distancia a cada zona

        alt Fuera de zona segura
            N8N->>DB: INSERT INTO alerts (zone_exit)
            N8N->>DB: SELECT device_token FROM devices<br/>WHERE user_id = monitors
            N8N->>FCM: POST FCM V1 (zone_exit alert)
            FCM->>FAM: Push Notification<br/>"Hijo ha salido de Casa"
        end

        N8N-->>REPO: {status: ok}
        REPO->>REPO: Actualizar lastLocationUpdate
    end
```

---

## Diagrama de Secuencia: Consulta de Ubicaciones Familiares

```mermaid
sequenceDiagram
    participant UI as MapScreen / FamilyScreen
    participant VM as ViewModel
    participant REPO as LocationRepository
    participant N8N as n8n (Family Locations)
    participant DB as PostgreSQL

    UI->>VM: onResume / Pull-to-refresh
    VM->>REPO: getFamilyLocations()
    REPO->>N8N: GET /api/family/locations

    N8N->>DB: SELECT users + devices + last location
    DB-->>N8N: Datos miembros familia
    N8N->>DB: SELECT safe_zones WHERE is_active
    DB-->>N8N: Zonas seguras

    N8N->>N8N: Format Response (Code node)
    N8N-->>REPO: {members: [...], safeZones: [...]}
    REPO-->>VM: FamilyData(members, safeZones)
    VM-->>UI: Actualizar UI state

    alt MapScreen
        UI->>UI: Renderizar marcadores + círculos
    else FamilyScreen
        UI->>UI: Renderizar tarjetas de miembros
    end
```

---

## Diagrama de Secuencia: Push Notification (FCM V1)

```mermaid
sequenceDiagram
    participant TRIGGER as Workflow Trigger<br/>(Location/Notify/Health)
    participant CODE as Code Node<br/>(JWT Generator)
    participant OAUTH as Google OAuth2
    participant FCM as FCM V1 API
    participant DEVICE as Dispositivo Android

    TRIGGER->>CODE: device_token + title + body
    CODE->>CODE: Leer /files/firebase-sa.json
    CODE->>CODE: Generar JWT con crypto<br/>(RS256, exp 1h)
    CODE-->>OAUTH: POST https://oauth2.googleapis.com/token<br/>{grant_type: jwt-bearer, assertion: JWT}
    OAUTH-->>CODE: {access_token}
    CODE->>FCM: POST https://fcm.googleapis.com/v1/<br/>projects/family-track-5548b/messages:send<br/>Authorization: Bearer {access_token}
    FCM-->>CODE: {name: "projects/.../messages/..."}
    FCM->>DEVICE: Push Notification
    DEVICE->>DEVICE: FamilyTrackMessagingService.onMessageReceived()
```

---

## Diagrama de Secuencia: Gestión de Zonas Seguras

```mermaid
sequenceDiagram
    participant UI as SafeZonesScreen
    participant VM as SafeZonesViewModel
    participant REPO as LocationRepository
    participant N8N_C as n8n (Create Zone)
    participant N8N_D as n8n (Delete Zone)
    participant DB as PostgreSQL

    Note over UI: Carga inicial
    UI->>VM: init
    VM->>REPO: getFamilyLocations()
    REPO-->>VM: FamilyData con safeZones
    VM-->>UI: Lista de zonas

    Note over UI: Crear zona
    UI->>VM: createSafeZone(name, lat, lng, radius)
    VM->>REPO: createSafeZone(...)
    REPO->>N8N_C: POST /api/safe-zones/create<br/>{name, lat, lng, radiusMeters, ...}
    N8N_C->>DB: INSERT INTO safe_zones
    DB-->>N8N_C: zone_id, name
    N8N_C-->>REPO: {success, zoneId, ...}
    REPO-->>VM: SafeZone
    VM-->>UI: Agregar zona a lista + Snackbar

    Note over UI: Eliminar zona
    UI->>VM: deleteSafeZone(zoneId)
    VM->>REPO: deleteSafeZone(zoneId)
    REPO->>N8N_D: POST /api/safe-zones/delete<br/>{zoneId}
    N8N_D->>DB: UPDATE safe_zones SET is_active=false
    N8N_D-->>REPO: {success}
    VM-->>UI: Remover zona de lista + Snackbar
```

---

## Diagrama de Secuencia: Backup Automático de n8n

```mermaid
sequenceDiagram
    participant CRON as Crontab (3:00 AM UTC)
    participant SCRIPT as backup.sh
    participant N8N as Container n8n
    participant FS as Filesystem IONOS

    CRON->>SCRIPT: Ejecutar diariamente
    SCRIPT->>SCRIPT: mkdir backup_YYYYMMDD_HHMMSS

    SCRIPT->>N8N: n8n export:workflow --all
    N8N-->>SCRIPT: workflows.json
    SCRIPT->>N8N: n8n export:credentials --all
    N8N-->>SCRIPT: credentials.json

    alt sqlite3 disponible
        SCRIPT->>N8N: sqlite3 .backup
        N8N-->>SCRIPT: database.sqlite
    else fallback
        SCRIPT->>N8N: docker cp database.sqlite
        N8N-->>SCRIPT: database.sqlite
    end

    SCRIPT->>FS: tar czf backup.tar.gz
    SCRIPT->>FS: rm -rf directorio temporal
    SCRIPT->>FS: Rotar: mantener últimos 30
    SCRIPT->>FS: Log en /var/log/n8n-backup.log
```

---

## Modelo de Datos (ER)

```mermaid
erDiagram
    USERS {
        int user_id PK
        varchar name
        varchar role
        int family_id
        timestamp created_at
    }

    DEVICES {
        int device_id PK
        int user_id FK
        varchar device_token
        varchar device_name
        int location_interval
        boolean is_active
        timestamp last_seen
        timestamp created_at
    }

    LOCATIONS {
        int location_id PK
        int device_id FK
        numeric latitude
        numeric longitude
        float accuracy
        timestamp timestamp
        timestamp received_at
    }

    SAFE_ZONES {
        int zone_id PK
        varchar name
        numeric center_lat
        numeric center_lng
        int radius_meters
        int monitored_user_id FK
        int created_by FK
        boolean is_active
        timestamp created_at
    }

    ALERTS {
        int alert_id PK
        varchar type
        int user_id FK
        int zone_id FK
        int device_id FK
        varchar message
        boolean acknowledged
        timestamp created_at
    }

    DEVICE_CONFIG {
        int config_id PK
        int device_id FK
        varchar key
        varchar value
        timestamp updated_at
    }

    NOTIFICATIONS_SENT {
        int id PK
        int device_id FK
        int from_user_id FK
        varchar title
        varchar message
        timestamp sent_at
    }

    USERS ||--o{ DEVICES : "tiene"
    DEVICES ||--o{ LOCATIONS : "reporta"
    USERS ||--o{ SAFE_ZONES : "monitoreado_en"
    USERS ||--o{ SAFE_ZONES : "creado_por"
    USERS ||--o{ ALERTS : "recibe"
    SAFE_ZONES ||--o{ ALERTS : "genera"
    DEVICES ||--o{ DEVICE_CONFIG : "configurado"
    DEVICES ||--o{ NOTIFICATIONS_SENT : "destino"
    USERS ||--o{ NOTIFICATIONS_SENT : "origen"
```

---

## Flujo de Navegación

```mermaid
graph TD
    START[App Launch] --> MA[MainActivity]
    MA --> NAV[FamilyTrackNavHost]
    NAV --> HOME[HomeScreen<br/>Mi Ubicación]
    NAV --> MAP[MapScreen<br/>Mapa Familiar]
    NAV --> FAM[FamilyScreen<br/>Mi Familia]
    NAV --> SET[SettingsScreen<br/>Ajustes]

    MAP -->|Shield FAB| SZ[SafeZonesScreen<br/>Zonas Seguras]
    SZ -->|Back| MAP

    HOME -.->|Toggle ubicación| LFS[LocationForegroundService]
    SET -.->|Toggle ubicación| LFS
    SET -.->|Cambiar intervalo| LFS

    style HOME fill:#e3f2fd
    style MAP fill:#e8f5e9
    style FAM fill:#fff3e0
    style SET fill:#f3e5f5
    style SZ fill:#fce4ec
```

---

## Stack Tecnológico Detallado

### App Android

| Capa | Tecnología | Versión |
|------|------------|---------|
| Lenguaje | Kotlin | 1.9.x |
| UI Framework | Jetpack Compose | BOM 2024.12.01 |
| Design System | Material 3 | (via BOM) |
| DI | Hilt | 2.51.1 |
| HTTP Client | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Serialización | Gson | 2.11.0 |
| Ubicación | Google Play Services Location | 21.3.0 |
| Mapas | Maps Compose | 6.1.0 |
| Notificaciones | Firebase Cloud Messaging | BOM 33.1.2 |
| Persistencia local | DataStore Preferences | 1.1.1 |
| Logging | Timber | 5.0.1 |
| Background | ForegroundService (tipo location) | - |
| Min API | 26 (Android 8.0) | - |
| Target API | 34 (Android 14) | - |
| JDK | 17 | Corretto/OpenJDK |
| Build | Gradle | 8.7 |

### Backend / Infraestructura

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Workflow Engine | n8n | 2.2.4 |
| Base de datos | PostgreSQL | 16 |
| Reverse Proxy | Traefik | v3 |
| Contenedores | Docker | 24+ |
| SSL | Let's Encrypt | auto-renew |
| Servidor | IONOS VPS | Ubuntu |
| Backup | Cron + bash script | diario 3AM |
