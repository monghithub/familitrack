#!/bin/bash
# n8n Backup Script
# Guarda workflows, credenciales y base de datos SQLite
# Ejecutar: crontab -e → 0 3 * * * /opt/apps/pro/n8n-backups/backup.sh >> /var/log/n8n-backup.log 2>&1

BACKUP_DIR="/opt/apps/pro/n8n-backups"
DATE=$(date +%Y%m%d_%H%M%S)
CURRENT="$BACKUP_DIR/$DATE"
CONTAINER="n8n"
MAX_BACKUPS=30  # mantener últimos 30 días

mkdir -p "$CURRENT"

# 1. Export workflows
docker exec $CONTAINER n8n export:workflow --all --output=/tmp/n8n-workflows-backup.json 2>/dev/null
docker cp $CONTAINER:/tmp/n8n-workflows-backup.json "$CURRENT/workflows.json" 2>/dev/null

# 2. Export credentials (encriptadas, pero restaurables)
docker exec $CONTAINER n8n export:credentials --all --output=/tmp/n8n-credentials-backup.json 2>/dev/null
docker cp $CONTAINER:/tmp/n8n-credentials-backup.json "$CURRENT/credentials.json" 2>/dev/null

# 3. Copia directa del SQLite (backup seguro con .backup si sqlite3 disponible, fallback a docker cp)
docker exec $CONTAINER sqlite3 /home/node/.n8n/database.sqlite ".backup /tmp/n8n-db-backup.sqlite" 2>/dev/null
if docker cp $CONTAINER:/tmp/n8n-db-backup.sqlite "$CURRENT/database.sqlite" 2>/dev/null; then
    docker exec $CONTAINER rm -f /tmp/n8n-db-backup.sqlite 2>/dev/null
else
    # Fallback: copia directa del archivo
    docker cp $CONTAINER:/home/node/.n8n/database.sqlite "$CURRENT/database.sqlite" 2>/dev/null
fi

# 4. Comprimir
cd "$BACKUP_DIR"
tar czf "$DATE.tar.gz" "$DATE" && rm -rf "$CURRENT"

# 5. Limpiar backups antiguos (mantener últimos MAX_BACKUPS)
ls -1t "$BACKUP_DIR"/*.tar.gz 2>/dev/null | tail -n +$((MAX_BACKUPS + 1)) | xargs -r rm -f

# 6. Limpiar archivos temporales en container
docker exec $CONTAINER rm -f /tmp/n8n-workflows-backup.json /tmp/n8n-credentials-backup.json 2>/dev/null

echo "[$(date)] Backup completado: $DATE.tar.gz"
