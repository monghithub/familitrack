# FamilyTrack - Guía de Configuración

## Requisitos Previos

| Requisito | Versión |
|-----------|---------|
| Java JDK | 17 (Corretto/OpenJDK) |
| Android SDK | 34 |
| Gradle | 8.7 |
| Dispositivo Android | API 26+ (Android 8.0+) |

---

## 1. Clonar el Repositorio

```bash
git clone git@github.com:monghithub/familitrack.git
cd familitrack
```

---

## 2. Instalar Android SDK (sin Android Studio)

### 2.1 Descargar Command Line Tools

```bash
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip cmdline-tools.zip
mv cmdline-tools latest
rm cmdline-tools.zip
```

### 2.2 Instalar componentes SDK

```bash
# Aceptar licencias
echo -e "y\ny\ny\ny\ny\ny\ny\ny" | ~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --licenses

# Instalar platform-tools, SDK 34 y build-tools
~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 2.3 Configurar local.properties

Crear archivo `local.properties` en la raíz del proyecto:

```properties
sdk.dir=/home/TU_USUARIO/Android/Sdk
MAPS_API_KEY=tu_google_maps_api_key
```

---

## 3. Configurar Google Maps API Key

### 3.1 Crear proyecto en Google Cloud Console

1. Ir a [console.cloud.google.com](https://console.cloud.google.com)
2. Crear un nuevo proyecto o usar existente

### 3.2 Habilitar Maps SDK

1. Menú lateral → **APIs y servicios → Biblioteca**
2. Buscar **"Maps SDK for Android"**
3. Click en **"Habilitar"**

### 3.3 Crear credenciales

1. Menú lateral → **APIs y servicios → Credenciales**
2. Click en **"+ Crear credenciales" → "Clave de API"**
3. Restringir la clave:
   - **Restricción de aplicación:** Apps de Android
   - **Package name:** `com.monghit.familytrack.debug`
   - **SHA-1:** Obtener con el comando:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

### 3.4 Agregar al proyecto

Agregar en `local.properties`:

```properties
MAPS_API_KEY=AIzaSy...tu_key
```

---

## 4. Configurar Firebase

### 4.1 Crear proyecto Firebase

1. Ir a [console.firebase.google.com](https://console.firebase.google.com)
2. Click en **"Agregar proyecto"**
3. Nombre: **FamilyTrack**
4. Google Analytics: opcional (se puede desactivar)

### 4.2 Registrar app Android

1. En el panel del proyecto, click en icono **Android**
2. Rellenar:
   - **Package name:** `com.monghit.familytrack`
   - **SHA-1 (opcional):** el obtenido en el paso 3.3
3. Descargar `google-services.json`
4. Copiar a `app/google-services.json`

### 4.3 Agregar client de debug

Editar `app/google-services.json` y agregar un segundo client con package `com.monghit.familytrack.debug`:

```json
{
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.monghit.familytrack"
        }
      }
    },
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.monghit.familytrack.debug"
        }
      }
    }
  ]
}
```

### 4.4 Habilitar Cloud Messaging

1. En Firebase Console → menú lateral → **Cloud Messaging**
2. Verificar que **API de Firebase Cloud Messaging (V1)** está habilitada

### Datos del proyecto actual

| Campo | Valor |
|-------|-------|
| Project ID | family-track-5548b |
| Sender ID | 411545008321 |
| FCM API | V1 habilitada |

---

## 5. Compilar el APK

### 5.1 Compilar APK de debug

```bash
export ANDROID_HOME=~/Android/Sdk
./gradlew assembleDebug
```

El APK se genera en: `app/build/outputs/apk/debug/app-debug.apk`

### 5.2 Compilar APK de release

```bash
./gradlew assembleRelease
```

> Nota: El release requiere configurar un keystore de firma.

---

## 6. Instalar en dispositivo Android

### 6.1 Habilitar depuración USB

1. **Ajustes → Acerca del teléfono** → tocar "Número de compilación" 7 veces
2. **Ajustes → Opciones de desarrollador** → activar "Depuración USB"
3. Conectar teléfono por USB
4. Aceptar el diálogo "¿Permitir depuración USB?"

### 6.2 Configurar permisos USB (Linux)

```bash
# Crear regla udev (ajustar idVendor según fabricante)
# Xiaomi: 2717 | Samsung: 04e8 | Google: 18d1 | Huawei: 12d1
echo 'SUBSYSTEM=="usb", ATTR{idVendor}=="2717", MODE="0666", GROUP="plugdev"' | sudo tee /etc/udev/rules.d/51-android.rules
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Desconectar y reconectar el teléfono.

### 6.3 Verificar conexión

```bash
~/Android/Sdk/platform-tools/adb devices
```

Debe mostrar el dispositivo.

### 6.4 Instalar

```bash
~/Android/Sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
```

Para reinstalar (actualizar):

```bash
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 7. Estructura del Proyecto

```
familytrack/
├── app/
│   ├── src/main/
│   │   ├── java/com/monghit/familytrack/
│   │   │   ├── FamilyTrackApp.kt              # Application + Hilt + NotificationChannels
│   │   │   ├── MainActivity.kt                # Activity principal (Compose)
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt               # Hilt DI (Retrofit, Repositories)
│   │   │   ├── data/
│   │   │   │   ├── remote/
│   │   │   │   │   ├── ApiService.kt          # Endpoints Retrofit
│   │   │   │   │   └── dto/ApiDtos.kt         # Request/Response DTOs
│   │   │   │   └── repository/
│   │   │   │       ├── LocationRepository.kt  # Lógica de ubicación
│   │   │   │       └── SettingsRepository.kt  # DataStore preferences
│   │   │   ├── domain/model/
│   │   │   │   ├── User.kt                    # Modelo de usuario
│   │   │   │   ├── Device.kt                  # Modelo de dispositivo
│   │   │   │   ├── Location.kt                # Modelo de ubicación
│   │   │   │   ├── SafeZone.kt                # Modelo de zona segura
│   │   │   │   ├── Alert.kt                   # Modelo de alerta
│   │   │   │   └── FamilyMember.kt            # Modelo compuesto
│   │   │   ├── services/
│   │   │   │   ├── LocationForegroundService.kt  # Servicio de ubicación
│   │   │   │   ├── FamilyTrackMessagingService.kt # FCM receiver
│   │   │   │   └── BootReceiver.kt               # Auto-start en boot
│   │   │   └── ui/
│   │   │       ├── navigation/
│   │   │       │   ├── NavRoutes.kt           # Rutas de navegación
│   │   │       │   └── FamilyTrackNavHost.kt  # NavHost + BottomBar
│   │   │       ├── screens/
│   │   │       │   ├── home/                  # Pantalla principal
│   │   │       │   ├── map/                   # Mapa familiar
│   │   │       │   ├── family/                # Lista de familiares
│   │   │       │   └── settings/              # Configuración
│   │   │       └── theme/                     # Material 3 theme
│   │   ├── res/                               # Recursos Android
│   │   └── AndroidManifest.xml
│   ├── google-services.json                   # Firebase config (no en git)
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml                     # Catálogo de versiones
│   └── wrapper/
├── build.gradle.kts                           # Root build config
├── settings.gradle.kts
├── local.properties                           # SDK path + API keys (no en git)
└── local.properties.example
```

---

## 8. Endpoints del Backend (n8n)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/register` | Registrar dispositivo |
| POST | `/api/location/update` | Enviar ubicación |
| POST | `/api/config/location-interval` | Cambiar intervalo |
| POST | `/api/notify` | Notificación manual |
| POST | `/api/send-push` | Enviar push notification (FCM V1) |
| CRON | Cada 24h | Health check dispositivos |

**URL de produccion:** `https://server.monghit.com/webhook/`

Configurado en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://server.monghit.com/webhook/\"")
```

### Infraestructura (IONOS)

- **PostgreSQL**: Container `familytrack-db` en red `traefik-net`
- **n8n**: Container `n8n` en red `traefik-net` (alcanza `familytrack-db:5432`)
- **Compose PostgreSQL**: `/opt/apps/pro/familytrack/docker-compose.yml`
- **Backups n8n**: `/opt/apps/pro/n8n-backups/backup.sh`
- **Firebase SA**: `/opt/n8n/local-files/firebase-sa.json` (montado en `/files/` dentro de n8n)

### Workflows n8n

| ID | Workflow | Path | Funcion |
|----|----------|------|---------|
| szQNpD8E8JA0NUq0 | Register Device | `POST /api/register` | Registra dispositivo con token FCM |
| 7AoJKxLu5XJUZmk6 | Location Update | `POST /api/location/update` | Guarda ubicacion + valida geofences |
| lio5OnRuAAeyJcYe | Config Interval | `POST /api/config/location-interval` | Cambia intervalo de reporte |
| Ra2UKsJkuqo3eLsk | Manual Notify | `POST /api/notify` | Envia notificacion entre familiares |
| W5xxwjdZMuPeT6xX | Health Check | CRON 24h | Detecta dispositivos offline |
| 0T5QZkWzblHs6z3N | Send Push | `POST /api/send-push` | Envia push notification via FCM V1 |

### Configuración del container n8n

Variables de entorno necesarias:

```bash
docker run -d \
  --name n8n \
  --restart unless-stopped \
  --network traefik-net \
  -v n8n_n8n_data:/home/node/.n8n \
  -v /opt/n8n/local-files:/files \
  -e WEBHOOK_URL=https://server.monghit.com/ \
  -e N8N_PORT=5678 \
  -e N8N_SECURE_COOKIE=true \
  -e GENERIC_TIMEZONE=Europe/Madrid \
  -e NODE_ENV=production \
  -e N8N_ENFORCE_SETTINGS_FILE_PERMISSIONS=true \
  -e N8N_HOST=server.monghit.com \
  -e N8N_RUNNERS_ENABLED=true \
  -e TZ=Europe/Madrid \
  -e N8N_PROTOCOL=https \
  -e NODE_FUNCTION_ALLOW_BUILTIN=crypto,fs \
  -l traefik.enable=true \
  -l 'traefik.http.routers.n8n.entrypoints=websecure' \
  -l 'traefik.http.routers.n8n.rule=Host(`n8n.monghit.com`) || Host(`server.monghit.com`)' \
  -l 'traefik.http.routers.n8n.tls.certresolver=letsencrypt' \
  -l 'traefik.http.services.n8n.loadbalancer.server.port=5678' \
  docker.n8n.io/n8nio/n8n
```

> **Importante:** `NODE_FUNCTION_ALLOW_BUILTIN=crypto,fs` es obligatoria para que el workflow Send Push pueda generar JWTs para la autenticación con Firebase.

### Workflow Send Push - Flujo

```
Webhook → Generate JWT (Code: crypto+fs) → Get Access Token (HTTP Request) → Send FCM (HTTP Request) → Response
```

El Code node solo genera el JWT con `crypto` y `fs` (lee la service account de `/files/firebase-sa.json`). Las llamadas HTTP (OAuth2 token exchange y envío FCM) se hacen con nodos HTTP Request nativos de n8n.

---

## 9. Gestión de Workflows n8n (CLI)

### Importar un workflow

```bash
docker exec n8n n8n import:workflow --input=/tmp/workflow.json
```

El JSON debe ser un **array** con campos obligatorios:

```json
[{
  "name": "Nombre del Workflow",
  "active": false,
  "isArchived": false,
  "versionId": "uuid-unico-aqui",
  "pinData": {},
  "meta": {},
  "staticData": null,
  "triggerCount": 0,
  "nodes": [...],
  "connections": {...},
  "settings": { "executionOrder": "v1" }
}]
```

### Publicar (activar) un workflow

```bash
docker exec n8n n8n publish:workflow --id=WORKFLOW_ID
docker restart n8n  # Necesario para que tome efecto
```

> **Problema conocido:** Si `publish:workflow` falla con **"Version not found"**, es porque la tabla `workflow_history` no tiene la entrada correspondiente. Ver sección de Troubleshooting.

### Listar workflows activos

```bash
docker exec n8n n8n list:workflow --active=true
```

---

## 10. Troubleshooting

### Gradle: App usa URL vieja tras cambiar BASE_URL

**Problema:** Tras cambiar `buildConfigField("String", "BASE_URL", ...)` en `build.gradle.kts`, el APK compilado sigue llamando a la URL anterior.

**Causa:** `BuildConfig.BASE_URL` es un `static final String` de Java. El compilador lo **inlinea** (copia literal) en todas las clases que lo referencian. El build cache de Gradle sirve las clases compiladas antiguas sin recompilarlas.

**Solución:**

```bash
./gradlew --no-build-cache --no-configuration-cache clean assembleDebug
```

> Siempre usar estos flags al cambiar campos de `buildConfigField`. Un `clean` simple no basta porque el build cache (`~/.gradle/caches/`) sigue sirviendo artefactos viejos.

---

### ADB: INSTALL_FAILED_USER_RESTRICTED

**Problema:** `adb install` falla con `INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`.

**Causa:** En Xiaomi (MIUI), la instalación via USB requiere aprobación explicita.

**Solución:**
1. **Ajustes → Opciones de desarrollador → Instalar via USB** → activar
2. La opción requiere estar logueado con cuenta Mi
3. Aceptar el diálogo de instalación que aparece en el teléfono
4. Usar flag `-t` para apps de test: `adb install -t app-debug.apk`

---

### ADB: No detecta el dispositivo

```bash
# Reiniciar ADB
~/Android/Sdk/platform-tools/adb kill-server
~/Android/Sdk/platform-tools/adb start-server

# Verificar que USB está conectado
lsusb | grep -i android
```

Configurar regla udev si es necesario:

```bash
# Xiaomi: 2717 | Samsung: 04e8 | Google: 18d1 | Huawei: 12d1
echo 'SUBSYSTEM=="usb", ATTR{idVendor}=="2717", MODE="0666", GROUP="plugdev"' | sudo tee /etc/udev/rules.d/51-android.rules
sudo udevadm control --reload-rules && sudo udevadm trigger
```

---

### n8n: "Version not found" al publicar workflow

**Problema:** `n8n publish:workflow --id=XXX` falla con `Version "..." not found for workflow "..."`.

**Causa:** La tabla `workflow_history` no contiene la entrada para el `versionId` del workflow importado. n8n requiere esta entrada para poder publicar.

**Solución:**

```bash
# 1. Detener n8n
docker stop n8n

# 2. Insertar la entrada en workflow_history copiando de workflow_entity
docker run --rm -v n8n_n8n_data:/data alpine:latest sh -c "
  apk add --no-cache sqlite > /dev/null 2>&1
  sqlite3 /data/database.sqlite \"
    INSERT INTO workflow_history (versionId, workflowId, nodes, connections, authors, createdAt, updatedAt)
    SELECT 'EL-VERSION-ID', id, nodes, connections, '[]', datetime('now'), datetime('now')
    FROM workflow_entity WHERE id='EL-WORKFLOW-ID';
  \"
  sqlite3 /data/database.sqlite 'PRAGMA wal_checkpoint(TRUNCATE);'
"

# 3. Iniciar n8n
docker start n8n
```

> **Importante:** Usar `INSERT...SELECT` para copiar nodes/connections. No intentar copiar el JSON manualmente en el SQL porque las comillas simples dentro del JSON rompen la query.

---

### n8n: "Module 'crypto' is disallowed" en Code node

**Problema:** Un Code node que usa `require('crypto')` o `require('fs')` falla con `Module 'crypto' is disallowed`.

**Causa:** n8n bloquea módulos nativos de Node.js en el sandbox del Code node por seguridad.

**Solución:** Añadir la variable de entorno al contenedor n8n:

```bash
-e NODE_FUNCTION_ALLOW_BUILTIN=crypto,fs
```

> Requiere recrear el contenedor. No se puede añadir env vars a un contenedor existente.

---

### n8n: "fetch is not defined" en Code node

**Problema:** Un Code node que usa `await fetch(url, ...)` falla con `fetch is not defined`.

**Causa:** El sandbox del Code node de n8n no expone la API `fetch` global, aunque Node.js 18+ la incluya.

**Solución:** No usar `fetch()` en Code nodes. En su lugar:
- Separar la lógica: usar Code node solo para procesamiento de datos (ej. generar JWT)
- Usar nodos **HTTP Request** nativos de n8n para las llamadas HTTP

Ejemplo del flujo Send Push:
```
Code node (genera JWT con crypto) → HTTP Request (intercambia JWT por access_token) → HTTP Request (envía FCM)
```

---

### n8n: Cambios en SQLite no toman efecto

**Problema:** Tras modificar directamente la base de datos SQLite de n8n, los cambios no se ven.

**Causa:** n8n usa SQLite con WAL (Write-Ahead Logging). Los cambios quedan en el archivo `.sqlite-wal` hasta que se hace checkpoint.

**Solución:**

```bash
# 1. Detener n8n (OBLIGATORIO antes de modificar SQLite)
docker stop n8n

# 2. Hacer las modificaciones con un container Alpine
docker run --rm -v n8n_n8n_data:/data alpine:latest sh -c "
  apk add --no-cache sqlite > /dev/null 2>&1
  sqlite3 /data/database.sqlite 'TU QUERY SQL AQUI;'
  sqlite3 /data/database.sqlite 'PRAGMA wal_checkpoint(TRUNCATE);'
"

# 3. Iniciar n8n
docker start n8n
```

> **Nunca** modificar SQLite mientras n8n está corriendo. Puede corromper la base de datos.

---

### n8n: Workflow usa credenciales PLACEHOLDER

**Problema:** Al importar workflows via CLI con IDs de credenciales temporales (ej. `"PLACEHOLDER"`), las ejecuciones fallan con `Credential with ID "PLACEHOLDER" does not exist`.

**Causa:** Los workflows tienen el ID de la credencial hardcodeado en los nodos que usan PostgreSQL u otros servicios.

**Solución:**

1. Crear la credencial manualmente en la UI de n8n
2. Obtener el ID real de la credencial (visible en la URL al editarla)
3. Actualizar **ambas tablas** (`workflow_entity` Y `workflow_history`):

```bash
docker stop n8n

docker run --rm -v n8n_n8n_data:/data alpine:latest sh -c "
  apk add --no-cache sqlite > /dev/null 2>&1

  # Actualizar workflow_entity
  sqlite3 /data/database.sqlite \"
    UPDATE workflow_entity
    SET nodes = REPLACE(nodes, 'PLACEHOLDER', 'ID_REAL')
    WHERE id = 'WORKFLOW_ID';
  \"

  # Actualizar workflow_history
  sqlite3 /data/database.sqlite \"
    UPDATE workflow_history
    SET nodes = REPLACE(nodes, 'PLACEHOLDER', 'ID_REAL')
    WHERE workflowId = 'WORKFLOW_ID';
  \"

  sqlite3 /data/database.sqlite 'PRAGMA wal_checkpoint(TRUNCATE);'
"

docker start n8n
```

> n8n cachea los workflows en memoria. Actualizar solo `workflow_entity` no basta; hay que actualizar también `workflow_history` y reiniciar.

---

### n8n: Recrear contenedor preservando datos

**Problema:** Necesitas añadir variables de entorno (ej. `NODE_FUNCTION_ALLOW_BUILTIN`) al contenedor n8n existente.

**Causa:** Docker no permite añadir env vars a un contenedor ya creado. Hay que recrearlo.

**Solución:**

```bash
# 1. Obtener configuración actual
docker inspect n8n --format '{{range .Config.Env}}{{println .}}{{end}}'
docker inspect n8n --format '{{json .Config.Labels}}'
docker inspect n8n --format '{{json .HostConfig.Binds}}'
docker inspect n8n --format '{{json .NetworkSettings.Networks}}'

# 2. Parar y eliminar
docker stop n8n && docker rm n8n

# 3. Recrear con TODOS los parámetros originales + los nuevos
docker run -d --name n8n --restart unless-stopped \
  --network traefik-net \
  -v n8n_n8n_data:/home/node/.n8n \
  -v /opt/n8n/local-files:/files \
  -e VARIABLE_ORIGINAL=valor \
  -e NUEVA_VARIABLE=valor \
  -l label.original=valor \
  docker.n8n.io/n8nio/n8n
```

> **Critico:** Los datos persisten en el volumen `n8n_n8n_data`. Al recrear el contenedor, NO se pierden workflows, credenciales ni ejecuciones. Solo hay que preservar los volúmenes, la red y las labels de Traefik.

---

### Firebase: No matching client for package name

El `google-services.json` debe incluir clients para ambos packages:
- `com.monghit.familytrack` (release)
- `com.monghit.familytrack.debug` (debug)

---

### Build: MAPS_API_KEY placeholder

Verificar que `local.properties` contiene:
```properties
MAPS_API_KEY=tu_api_key
```

---

### Build: Java version

Verificar Java 17:
```bash
java -version
```

Si usas SDKMAN:
```bash
sdk use java 17.0.17-amzn
```
