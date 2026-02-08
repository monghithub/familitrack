# Feature #16: Chat Familiar

> **Issue:** [#27](https://github.com/monghithub/familitrack/issues/27) - Chat familiar
> **Estado:** Completada

## Descripción

Chat grupal en tiempo real para toda la familia. Permite enviar mensajes de texto, compartir ubicación, y recibir mensajes mediante polling cada 10 segundos. Incluye historial de conversación y visualización de quién escribió cada mensaje.

## Componentes

| Archivo | Función |
|---------|---------|
| `ChatScreen.kt` | UI con historial de chat e input |
| `ChatViewModel.kt` | Estado, polling y envío de mensajes |
| `ApiDtos.kt` | `SendChatMessageRequest/Response`, `ChatMessagesResponse` |
| `ApiService.kt` | Endpoints `sendChatMessage()`, `getChatMessages()` |
| `LocationRepository.kt` | Métodos `sendChatMessage()`, `getChatMessages()` |

## Características

### Historial de Chat
- LazyColumn con mensajes ordenados cronológicamente
- Burbujas con nombre del autor + hora + contenido
- Alineación alternada (izq para otros, dcha para user actual)
- Auto-scroll al final cuando llegan nuevos mensajes

### Envío de Mensajes
- TextField editable con icono de envío
- Botón de ubicación para compartir posición
- Validación: no enviar si está vacío
- Loading visual mientras se envía

### Polling de Mensajes
- Petición al backend cada 10 segundos
- Se obtienen mensajes nuevos desde último timestamp
- Se detiene cuando screen está en background

## API Endpoints

### Enviar Mensaje
```json
POST /api/chat/send

{
    "familyId": 10,
    "userId": 1,
    "message": "¡Hola a todos!",
    "latitude": null,
    "longitude": null
}

Response:
{
    "success": true,
    "messageId": 1234,
    "createdAt": "2025-02-08T14:30:00Z"
}
```

### Obtener Mensajes
```json
POST /api/chat/messages

{
    "familyId": 10,
    "limit": 100,
    "since": "2025-02-08T14:20:00Z"
}

Response:
{
    "messages": [
        {
            "messageId": 1234,
            "fromUser": "Juan",
            "message": "¡Hola!",
            "createdAt": "2025-02-08T14:30:00Z"
        }
    ]
}
```

## Tabla chat_messages

```sql
CREATE TABLE chat_messages (
    id SERIAL PRIMARY KEY,
    family_id INT REFERENCES families(id),
    user_id INT REFERENCES users(id),
    message TEXT NOT NULL,
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Validaciones

| Campo | Validación |
|-------|-----------|
| Mensaje | 1-500 caracteres |
| Usuario | Debe pertenecer a la familia |
| Familia | Debe existir y estar activa |

## Notas Técnicas

- El polling causa latencia de hasta 10 segundos
- No hay notificaciones push de chat
- El historial es indefinido (sin límite de antigüedad)
- Las conexiones lentas pueden causar retrasos

