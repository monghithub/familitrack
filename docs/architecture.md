# Arquitectura de FamilyTrack

## Visión General

FamilyTrack es una aplicación de geolocalización familiar compuesta por una **app Android nativa** y un **backend serverless** basado en n8n + PostgreSQL, desplegado en Docker sobre un servidor IONOS.

**v2.0** incluye 14 pantallas, 20 endpoints/workflows, 10+ tablas de base de datos, autenticación por PIN/biometría, chat familiar, SOS de emergencia, historial de rutas, modo oscuro e internacionalización.

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
            N8N[n8n<br/>20 Workflows]
            PG[PostgreSQL 16<br/>10+ tablas]
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
    subgraph "UI Layer (Jetpack Compose) - 14 Screens"
        SPLASH[SplashScreen]
        ONB[OnboardingScreen]
        FSETUP[FamilySetupScreen]
        PIN[PinScreen]
        HS[HomeScreen]
        MS[MapScreen]
        FS[FamilyScreen]
        SS[SettingsScreen]
        SZS[SafeZonesScreen]
        PROF[ProfileScreen]
        CHAT[ChatScreen]
        PHOTOS[PhotosScreen]
        ROUTE[RouteHistoryScreen]
        PERM[PermissionsScreen]
        NAV[FamilyTrackNavHost<br/>Bottom Navigation]
    end

    subgraph "ViewModel Layer"
        FSVM[FamilySetupViewModel]
        PINVM[PinViewModel]
        HVM[HomeViewModel]
        MVM[MapViewModel]
        FVM[FamilyViewModel]
        SVM[SettingsViewModel]
        SZVM[SafeZonesViewModel]
        PROFVM[ProfileViewModel]
        CHATVM[ChatViewModel]
        PHOTVM[PhotosViewModel]
        ROUTVM[RouteHistoryViewModel]
    end

    subgraph "Data Layer"
        LR[LocationRepository]
        SR[SettingsRepository<br/>DataStore]
        SEC[SecurityRepository<br/>EncryptedSharedPreferences]
        AS[ApiService<br/>Retrofit - 20 endpoints]
    end

    subgraph "Services"
        LFS[LocationForegroundService<br/>FusedLocationProvider]
        FCMS[FamilyTrackMessagingService<br/>FCM Receiver]
        BR[BootReceiver]
        BIO[BiometricHelper<br/>BiometricPrompt]
    end

    subgraph "Domain"
        USER[User]
        DEV[Device]
        LOC[Location]
        SZ[SafeZone]
        ALT[Alert]
        FM[FamilyMember]
    end

    NAV --> SPLASH --> ONB --> FSETUP --> PIN --> HS
    NAV --> HS & MS & FS & SS
    MS --> SZS
    SS --> PROF & CHAT & PERM

    FSETUP --> FSVM
    PIN --> PINVM
    HS --> HVM
    MS --> MVM
    FS --> FVM
    SS --> SVM
    SZS --> SZVM
    PROF --> PROFVM
    CHAT --> CHATVM
    PHOTOS --> PHOTVM
    ROUTE --> ROUTVM

    FSVM & HVM & MVM & FVM & SVM & SZVM --> LR
    HVM & SVM & FSVM & CHATVM & PROFVM & PHOTVM & ROUTVM --> SR
    PINVM & SVM --> SEC
    PROFVM & CHATVM & PHOTVM & ROUTVM --> AS
    LR --> AS
    LFS --> LR
    LFS --> SR
    PIN --> BIO
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
        W10[POST /api/family/create]
        W11[POST /api/family/join]
        W12[POST /api/user/update-profile]
        W13[POST /api/user/profile]
        W14[POST /api/quick-message]
        W15[POST /api/emergency]
        W16[GET /api/locations/history]
        W17[POST /api/chat/send]
        W18[POST /api/chat/messages]
        W19[POST /api/photos/send]
        W20[POST /api/photos/list]
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
        WF10[Create Family]
        WF11[Join Family]
        WF12[Update Profile]
        WF13[Get Profile]
        WF14[Quick Message]
        WF15[Emergency SOS]
        WF16[Location History]
        WF17[Chat Send]
        WF18[Chat Messages]
        WF19[Photo Send]
        WF20[Photo List]
    end

    subgraph "PostgreSQL"
        T1[(users)]
        T2[(devices)]
        T3[(locations)]
        T4[(safe_zones)]
        T5[(alerts)]
        T6[(device_config)]
        T7[(notifications_sent)]
        T8[(families)]
        T9[(messages)]
        T10[(photos)]
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
    W10 --> WF10 --> T8 & T1
    W11 --> WF11 --> T8 & T1
    W12 --> WF12 --> T1
    W13 --> WF13 --> T1 & T8
    W14 --> WF14 --> T7
    W15 --> WF15 --> T5
    W16 --> WF16 --> T3
    W17 --> WF17 --> T9
    W18 --> WF18 --> T9
    W19 --> WF19 --> T10
    W20 --> WF20 --> T10

    WF2 -->|Geofence alert| WF6
    WF5 -->|Send push| WF6
    WF9 -->|Offline alert| WF6
    WF14 -->|Send push| WF6
    WF15 -->|SOS push| WF6
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
    participant BAT as BatteryManager
    participant REPO as LocationRepository
    participant N8N as n8n (Location Update)
    participant DB as PostgreSQL
    participant FCM as Firebase FCM
    participant FAM as App Familiar

    loop Cada N minutos (configurable)
        GPS->>SVC: Nueva ubicación (lat, lng, accuracy)
        BAT->>SVC: batteryLevel, isCharging
        SVC->>REPO: sendLocation(location, battery)
        REPO->>N8N: POST /api/location/update<br/>{userId, lat, lng, accuracy,<br/>batteryLevel, isCharging}
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

    N8N->>DB: SELECT users + devices + last location + battery
    DB-->>N8N: Datos miembros familia
    N8N->>DB: SELECT safe_zones WHERE is_active
    DB-->>N8N: Zonas seguras

    N8N->>N8N: Format Response (Code node)
    N8N-->>REPO: {members: [...], safeZones: [...]}
    REPO-->>VM: FamilyData(members, safeZones)
    VM-->>UI: Actualizar UI state

    alt MapScreen
        UI->>UI: Renderizar marcadores + círculos + batería
    else FamilyScreen
        UI->>UI: Renderizar tarjetas con batería
    end
```

---

## Diagrama de Secuencia: Push Notification (FCM V1)

```mermaid
sequenceDiagram
    participant TRIGGER as Workflow Trigger<br/>(Location/Notify/Health/SOS)
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

## Diagrama de Secuencia: Registro de Familia (v2.0)

```mermaid
sequenceDiagram
    participant UI as FamilySetupScreen
    participant VM as FamilySetupViewModel
    participant REPO as LocationRepository
    participant N8N as n8n (Create/Join Family)
    participant DB as PostgreSQL
    participant DS as DataStore

    Note over UI: Flujo "Crear familia"
    UI->>VM: createFamily(familyName, userName)
    VM->>REPO: createFamily(familyName, userName)
    REPO->>N8N: POST /api/family/create<br/>{familyName, userName}
    N8N->>DB: INSERT INTO families (name, invite_code)
    N8N->>DB: INSERT INTO users (name, role=admin, family_id)
    DB-->>N8N: userId, familyId, inviteCode
    N8N-->>REPO: {success, userId, familyId, inviteCode, role}
    REPO->>DS: Guardar userId, familyId, inviteCode, role
    VM-->>UI: Mostrar inviteCode (6 chars) + copiar al portapapeles

    Note over UI: Flujo "Unirme a familia"
    UI->>VM: joinFamily(inviteCode, userName)
    VM->>REPO: joinFamily(inviteCode, userName)
    REPO->>N8N: POST /api/family/join<br/>{inviteCode, userName}
    N8N->>DB: SELECT families WHERE invite_code = ?
    N8N->>DB: INSERT INTO users (name, role=monitored, family_id)
    DB-->>N8N: userId, familyId, familyName
    N8N-->>REPO: {success, userId, familyId, familyName, role}
    REPO->>DS: Guardar userId, familyId, role
    VM-->>UI: Navegar a HomeScreen
```

---

## Diagrama de Secuencia: Autenticación PIN + Biométrica (v2.0)

```mermaid
sequenceDiagram
    participant UI as PinScreen
    participant VM as PinViewModel
    participant SEC as SecurityRepository
    participant ESP as EncryptedSharedPreferences
    participant BIO as BiometricHelper
    participant BP as BiometricPrompt

    Note over UI: Primer uso (crear PIN)
    UI->>VM: onDigit(4 dígitos)
    VM->>VM: mode = CREATE → CONFIRM
    UI->>VM: onDigit(4 dígitos repetidos)
    VM->>SEC: setPin(pin)
    SEC->>SEC: SHA-256 hash
    SEC->>ESP: putString("pin_hash", hash)
    VM-->>UI: isAuthenticated = true

    Note over UI: Uso posterior (verificar)
    UI->>VM: init
    VM->>SEC: isPinSet() → true
    VM->>SEC: isBiometricEnabled() → true
    VM-->>UI: mode = VERIFY, biometricAvailable = true

    alt Biometría disponible
        UI->>BIO: canUseBiometric(activity)
        BIO-->>UI: true
        UI->>BIO: authenticate(activity)
        BIO->>BP: BiometricPrompt.authenticate()
        BP-->>BIO: onAuthenticationSucceeded
        BIO-->>VM: onBiometricSuccess()
        VM-->>UI: isAuthenticated = true
    else PIN manual
        UI->>VM: onDigit(4 dígitos)
        VM->>SEC: verifyPin(pin)
        SEC->>SEC: SHA-256 hash + comparar
        SEC-->>VM: true/false
        VM-->>UI: isAuthenticated o error
    end
```

---

## Diagrama de Secuencia: Chat Familiar (v2.0)

```mermaid
sequenceDiagram
    participant UI as ChatScreen
    participant VM as ChatViewModel
    participant API as ApiService
    participant N8N as n8n (Chat)
    participant DB as PostgreSQL
    participant DS as DataStore

    Note over UI: Carga inicial + polling
    UI->>VM: init
    VM->>DS: familyId, userId
    VM->>API: POST /api/chat/messages<br/>{familyId, limit: 50}
    API->>N8N: Webhook
    N8N->>DB: SELECT messages + users<br/>WHERE family_id = ? ORDER BY created_at DESC
    DB-->>N8N: Lista de mensajes
    N8N-->>API: {messages: [{id, content, createdAt, userId, userName}]}
    API-->>VM: ChatMessagesResponse
    VM->>VM: Mapear a ChatMessage (isOwnMessage)
    VM-->>UI: Renderizar burbujas

    loop Cada 10 segundos
        VM->>API: POST /api/chat/messages
        API-->>VM: Mensajes actualizados
        VM-->>UI: Actualizar lista
    end

    Note over UI: Enviar mensaje
    UI->>VM: sendMessage()
    VM->>API: POST /api/chat/send<br/>{fromUserId, content}
    API->>N8N: Webhook
    N8N->>DB: INSERT INTO messages
    N8N-->>API: {success, messageId}
    VM->>VM: loadMessages() (refrescar)
    VM-->>UI: Scroll al último mensaje
```

---

## Diagrama de Secuencia: SOS de Emergencia (v2.0)

```mermaid
sequenceDiagram
    participant UI as HomeScreen
    participant VM as HomeViewModel
    participant API as ApiService
    participant N8N as n8n (Emergency)
    participant DB as PostgreSQL
    participant PUSH as Send Push Workflow
    participant FCM as Firebase FCM
    participant FAM as Apps Familiares

    UI->>UI: Tap FAB rojo "SOS"
    UI->>UI: AlertDialog confirmación
    UI->>VM: sendSos(latitude, longitude)
    VM->>DS: userId
    VM->>API: POST /api/emergency<br/>{userId, latitude, longitude}
    API->>N8N: Webhook
    N8N->>DB: INSERT INTO alerts (type=sos, user_id, lat, lng)
    N8N->>DB: SELECT device_tokens<br/>FROM devices WHERE family_id = ?
    N8N->>PUSH: Para cada familiar
    PUSH->>FCM: POST FCM V1<br/>"SOS de [Nombre]"
    FCM->>FAM: Push Notification urgente
    N8N-->>API: {success, alertId}
    API-->>VM: EmergencyResponse
    VM-->>UI: Snackbar "SOS enviado a tu familia"
```

---

## Diagrama de Secuencia: Historial de Rutas (v2.0)

```mermaid
sequenceDiagram
    participant UI as RouteHistoryScreen
    participant VM as RouteHistoryViewModel
    participant API as ApiService
    participant N8N as n8n (Location History)
    participant DB as PostgreSQL

    UI->>VM: init (fecha = hoy)
    VM->>API: GET /api/locations/history<br/>?userId=X&date=YYYY-MM-DD
    API->>N8N: Webhook
    N8N->>DB: SELECT latitude, longitude, accuracy, timestamp<br/>FROM locations WHERE device_id IN<br/>(SELECT id FROM devices WHERE user_id = ?)<br/>AND DATE(timestamp) = ?<br/>ORDER BY timestamp ASC
    DB-->>N8N: Lista de puntos
    N8N-->>API: {locations: [{lat, lng, accuracy, timestamp}]}
    API-->>VM: LocationHistoryResponse
    VM-->>UI: Renderizar Polyline en GoogleMap + contador

    Note over UI: Cambiar fecha
    UI->>VM: selectDate("2026-02-07")
    VM->>API: GET /api/locations/history?date=2026-02-07
    API-->>VM: Puntos del día seleccionado
    VM-->>UI: Actualizar mapa con nueva ruta
```

---

## Modelo de Datos (ER)

```mermaid
erDiagram
    FAMILIES {
        int family_id PK
        varchar name
        varchar invite_code
        timestamp created_at
    }

    USERS {
        int user_id PK
        varchar name
        varchar role
        varchar avatar
        int family_id FK
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
        int battery_level
        boolean is_charging
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
        numeric latitude
        numeric longitude
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

    MESSAGES {
        int message_id PK
        int user_id FK
        int family_id FK
        text content
        timestamp created_at
    }

    PHOTOS {
        int photo_id PK
        int from_user_id FK
        int to_user_id FK
        text image_data
        varchar caption
        timestamp created_at
    }

    FAMILIES ||--o{ USERS : "tiene"
    USERS ||--o{ DEVICES : "tiene"
    DEVICES ||--o{ LOCATIONS : "reporta"
    USERS ||--o{ SAFE_ZONES : "monitoreado_en"
    USERS ||--o{ SAFE_ZONES : "creado_por"
    USERS ||--o{ ALERTS : "recibe"
    SAFE_ZONES ||--o{ ALERTS : "genera"
    DEVICES ||--o{ DEVICE_CONFIG : "configurado"
    DEVICES ||--o{ NOTIFICATIONS_SENT : "destino"
    USERS ||--o{ NOTIFICATIONS_SENT : "origen"
    FAMILIES ||--o{ MESSAGES : "contiene"
    USERS ||--o{ MESSAGES : "envía"
    USERS ||--o{ PHOTOS : "envía"
    USERS ||--o{ PHOTOS : "recibe"
```

---

## Flujo de Navegación

```mermaid
graph TD
    START[App Launch] --> MA[MainActivity]
    MA --> SPLASH[SplashScreen<br/>1.5s fade-in]

    SPLASH --> CHECK{Verificar estado}

    CHECK -->|onboarding no completado| ONB[OnboardingScreen<br/>3 páginas]
    CHECK -->|familyId == 0| FSETUP[FamilySetupScreen<br/>Crear / Unirme]
    CHECK -->|PIN configurado| PIN[PinScreen<br/>PIN + Biometría]
    CHECK -->|todo listo| HOME

    ONB -->|Completar / Skip| FSETUP
    FSETUP -->|Setup completo| HOME
    PIN -->|Autenticado| HOME

    HOME[HomeScreen<br/>Mi Ubicación + SOS + Quick Messages] --> |Bottom Nav| MAP
    HOME --> |Bottom Nav| FAM
    HOME --> |Bottom Nav| SET

    MAP[MapScreen<br/>Mapa Familiar] --> |Bottom Nav| HOME
    MAP --> |Bottom Nav| FAM
    MAP --> |Bottom Nav| SET
    MAP -->|Shield FAB| SZ[SafeZonesScreen]

    FAM[FamilyScreen<br/>Mi Familia] --> |Bottom Nav| HOME
    FAM --> |Bottom Nav| MAP
    FAM --> |Bottom Nav| SET

    SET[SettingsScreen<br/>Ajustes] --> |Bottom Nav| HOME
    SET --> |Bottom Nav| MAP
    SET --> |Bottom Nav| FAM
    SET -->|Editar perfil| PROF[ProfileScreen]
    SET -->|Chat familiar| CHAT[ChatScreen]
    SET -->|Permisos| PERM[PermissionsScreen]

    SZ -->|Back| MAP
    PROF -->|Back| SET
    CHAT -->|Back| SET
    PERM -->|Back| SET

    style SPLASH fill:#e1f5fe
    style ONB fill:#f3e5f5
    style FSETUP fill:#fff3e0
    style PIN fill:#fce4ec
    style HOME fill:#e3f2fd
    style MAP fill:#e8f5e9
    style FAM fill:#fff3e0
    style SET fill:#f3e5f5
    style SZ fill:#fce4ec
    style PROF fill:#e0f2f1
    style CHAT fill:#f1f8e9
    style PERM fill:#fbe9e7
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
| Seguridad | EncryptedSharedPreferences | security-crypto 1.1.0-alpha06 |
| Biometría | BiometricPrompt | biometric 1.2.0-alpha05 |
| Permisos | Accompanist Permissions | 0.34.0 |
| i18n | stringResource() | EN + ES |
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
