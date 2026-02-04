# FamilyTrack

Aplicación Android para compartir ubicación en tiempo real entre miembros de una familia.

## Funcionalidades

- **Compartir ubicación**: Envío periódico configurable (1-60 min)
- **Zonas seguras**: Geofences con alertas de entrada/salida
- **Notificaciones push**: Alertas cuando alguien sale de zona segura
- **Mapa familiar**: Visualización de ubicaciones de todos los miembros
- **Health check**: Detección de dispositivos offline

## Stack Técnico

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Ubicación | FusedLocationProvider |
| Notificaciones | Firebase Cloud Messaging |
| Red | Retrofit + OkHttp |
| DB Local | Room |
| Background | ForegroundService + WorkManager |

## Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- Android SDK 34
- JDK 17
- Cuenta Firebase con FCM habilitado

## Configuración

1. Clonar el repositorio
2. Abrir en Android Studio
3. Agregar `google-services.json` de Firebase en `app/`
4. Configurar la URL del backend en `app/src/main/res/values/strings.xml`
5. Sincronizar Gradle y ejecutar

## Arquitectura

```
app/
├── data/           # Repositorios, Room DB, API
├── domain/         # Modelos y casos de uso
├── services/       # LocationService, FCM Service
├── ui/             # Compose screens y ViewModels
└── utils/          # Helpers y extensiones
```

## Backend

El backend está implementado en n8n con PostgreSQL. Ver documentación en `/docs`.

## Licencia

Uso privado - Proyecto familiar
