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
| CRON | Cada 24h | Health check dispositivos |

**URL de produccion:** `https://server.monghit.com/webhook/`

Configurado en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://server.monghit.com/webhook/\"")
```

### Infraestructura (IONOS)

- **PostgreSQL**: Container `familytrack-db` en red `traefik-net`
- **n8n**: Container `n8n` en red `traefik-net` (alcanza `familytrack-db:5432`)
- **Compose**: `/opt/apps/pro/familytrack/docker-compose.yml`
- **Backups n8n**: `/opt/apps/pro/n8n-backups/backup.sh`

### Workflows n8n

| Workflow | Path | Funcion |
|----------|------|---------|
| Register Device | `POST /api/register` | Registra dispositivo con token FCM |
| Location Update | `POST /api/location/update` | Guarda ubicacion + valida geofences |
| Config Interval | `POST /api/config/location-interval` | Cambia intervalo de reporte |
| Manual Notify | `POST /api/notify` | Envia notificacion entre familiares |
| Health Check | CRON 24h | Detecta dispositivos offline |

---

## 9. Troubleshooting

### ADB no detecta el dispositivo

```bash
# Reiniciar ADB
~/Android/Sdk/platform-tools/adb kill-server
~/Android/Sdk/platform-tools/adb start-server

# Verificar que USB está conectado
lsusb | grep -i android
```

### Error: No matching client for package name

El `google-services.json` debe incluir clients para ambos packages:
- `com.monghit.familytrack` (release)
- `com.monghit.familytrack.debug` (debug)

### Error: MAPS_API_KEY placeholder

Verificar que `local.properties` contiene:
```properties
MAPS_API_KEY=tu_api_key
```

### Build fails con Java version

Verificar Java 17:
```bash
java -version
```

Si usas SDKMAN:
```bash
sdk use java 17.0.17-amzn
```
