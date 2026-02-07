#!/bin/bash
# Setup script para instalar el cron de backup de n8n
# Ejecutar una sola vez en el servidor IONOS

BACKUP_DIR="/opt/apps/pro/n8n-backups"
SCRIPT="$BACKUP_DIR/backup.sh"
LOG="/var/log/n8n-backup.log"

# Copiar script de backup
cp backup.sh "$SCRIPT"
chmod +x "$SCRIPT"

# Crear directorio de backups si no existe
mkdir -p "$BACKUP_DIR"

# Crear log file
touch "$LOG"

# Instalar cron job (3:00 AM UTC diario)
(crontab -l 2>/dev/null | grep -v "n8n-backup"; echo "# n8n backup diario a las 3:00 AM UTC"; echo "0 3 * * * $SCRIPT >> $LOG 2>&1") | crontab -

echo "Cron instalado. Verificar con: crontab -l"
echo "Logs en: $LOG"
echo "Backups en: $BACKUP_DIR"
