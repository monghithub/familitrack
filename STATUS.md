# FamilyTrack v2.0 - Estado del Proyecto (2026-02-08)

## ✅ COMPLETADO

### Backend (n8n)
- ✅ 15 workflows creados (6 nuevos v2.0 + 9 existentes v1.0)
- ✅ Workflows optimizados con CTEs y queries eficientes
- ✅ Todos los JSONs listos en `infra/n8n-workflows/`
- ✅ Archivos validados (JSON válido)

### Base de Datos
- ✅ Schema v2.0 completo (12 tablas)
- ✅ Tablas nuevas: families, user_families, chat_messages, photos, messages
- ✅ Índices de performance agregados
- ✅ Reset automation: `reset-database.sh`
- ✅ Seed data con 6 usuarios + 1 familia + 3 zonas + chat/fotos

### Android App
- ✅ 14 pantallas implementadas
- ✅ Material 3 + Jetpack Compose
- ✅ DTOs y API endpoints definidos
- ✅ Servicios: LocationForegroundService, FCM, Geofencing
- ✅ APK compilada e instalada
- ✅ Credenciales: SHA-1, Google Maps, Firebase

### Documentación
- ✅ 6 feature docs (features 11-16)
- ✅ Architecture.md con diagramas
- ✅ README-RESET.md con instrucciones

### Git
- ✅ Commits organizados por feature
- ✅ CLAUDE.md con instrucciones
- ✅ ADB tools documentadas

---

## ⏳ PENDIENTE

### 1. CRÍTICO: Importar workflows en n8n
**Estado**: Workflows creados pero no importados en n8n
**Razón**: Locks de SQLite en n8n impiden importación automática
**Solución**: Importar manualmente via UI web

**Pasos**:
1. Abre https://n8n.monghit.com
2. Menu (≡) → Workflows
3. Presiona "Import" (arriba a la derecha)
4. Selecciona folder: `~/Git/personal/monghithub/apk_android/familytrack/infra/n8n-workflows/`
5. Carga todos los 15 .json

**Tiempo estimado**: 2 minutos

### 2. Testing E2E en móvil
**Después de importar workflows**:
1. Cierra app en móvil (Ctrl+C o force stop)
2. Abre app → Onboarding → Family Setup
3. Toca "Create New Family"
4. Completa form → "Create"
5. Verifica sin error JSON (debe devolver familyId, inviteCode, etc.)

---

## 📊 Resumen de Archivos

### Workflows (15 total)
```
infra/n8n-workflows/
├── 06-send-push.json              # v1.0 - Send FCM notifications
├── 07-family-locations-v2.json    # v1.0 - Get family locations
├── 08-create-safe-zone.json       # v1.0 - Create geofence
├── 09-delete-safe-zone.json       # v1.0 - Delete geofence
├── 10-family-create.json          # v2.0 ✨ - Create family + admin user
├── 11-family-join.json            # v2.0 ✨ - Join family by code
├── 12-user-profile.json           # v2.0 ✨ - Get user profile
├── 13-user-profile-update.json    # v2.0 ✨ - Update profile
├── 14-chat-send.json              # v2.0 ✨ - Send chat message
├── 15-chat-messages.json          # v2.0 ✨ - Get chat history
├── 16-quick-message.json          # v2.0 ✨ - Send quick status
├── 17-emergency.json              # v2.0 ✨ - SOS panic button
├── 18-photo-send.json             # v2.0 ✨ - Share photo
├── 19-photo-list.json             # v2.0 ✨ - Get photos
└── 20-location-history.json       # v2.0 ✨ - Route history by date
```

### Database
```
infra/init/
├── 00-reset-db.sql                # Drop all tables
├── 01-schema-v2.sql               # Create 12 tables
├── 02-seed-v2.sql                 # Insert test data
├── reset-database.sh              # Automation script (Docker-aware)
└── README-RESET.md                # Usage guide + troubleshooting
```

### Documentation
```
docs/
├── architecture.md                # 10 Mermaid diagrams
├── features/
│   ├── 11-family-registration.md
│   ├── 12-family-joining.md
│   ├── 13-user-profiles.md
│   ├── 14-family-chat.md
│   ├── 15-photo-sharing.md
│   └── 16-quick-messages.md
```

---

## 🔧 Herramientas Locales

### ADB (Android Debug Bridge)
- **Ruta**: `~/Android/Sdk/platform-tools/adb`
- **Usar**: `~/Android/Sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk`

### Git Commits Recientes
```
094b9ed - fix(n8n): simplify workflows 10-13 with single SQL queries
00a0bca - fix(n8n): correct SQL queries and database schema references
accb0dc - docs: add ADB tools and common commands
```

---

## 📱 Flujo de Usuario (Esperado)

1. **App inicia** → Splash → Onboarding (3 páginas)
2. **Family Setup** → Opción: Create Family o Join Family
3. **Create Family** → Rellena familyName + userName → POST /api/family/create
4. **Success** → App crea familia, obtiene familyId + inviteCode + role
5. **Home Screen** → Muestra ubicaciones familiares, miembros, chat, fotos

---

## 🚀 Próximos Pasos

1. **Importar workflows en n8n** (2 min, manual)
2. **Test Create Family en app** (30 seg)
3. **Test Join Family con código** (30 seg)
4. **Test Chat/Fotos/Quick Messages** (2 min)
5. **Verificar Geofencing** (5 min en exteriores)

**Tiempo total**: ~15 minutos

---

## 🐛 Notas Técnicas

### Workflows v2.0
- Usan CTEs (Common Table Expressions) para transacciones ACID
- Simplifican lógica: 1 query PostgreSQL + 1 Code node + 1 Response node
- Evitan referencias complejas entre nodos (que causaban "Active version not found")

### Database
- Soft deletes via `is_active` flag
- `user_families` junction table para multi-familia support
- Índices en búsquedas frecuentes (timestamps, family_id, user_id)

### App
- FCM v1 API (Google deprecated v0)
- Firebase Service Account en n8n para generar access tokens
- Kotlin Coroutines + Flows para async operations

---

**Último update**: 2026-02-08 20:45 UTC
**Estado**: v2.0 Feature-complete, Pendiente: n8n import + E2E testing
