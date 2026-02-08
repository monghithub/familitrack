#!/bin/bash

# FamilyTrack Database Reset Script
# Este script borra completamente la BD y la recrea con datos de ejemplo

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
DB_USER="${DB_USER:-familytrack}"
DB_NAME="${DB_NAME:-familytrack}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         FamilyTrack Database Reset Script v2.0         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Mostrar configuración
echo -e "${YELLOW}Configuración:${NC}"
echo "  Database: ${DB_NAME}"
echo "  User: ${DB_USER}"
echo "  Host: ${DB_HOST}:${DB_PORT}"
echo ""

# Confirmación
echo -e "${RED}⚠️  ADVERTENCIA: Esto borrará TODOS los datos de la base de datos${NC}"
echo -e "${RED}⚠️  Esta acción NO se puede deshacer${NC}"
echo ""
read -p "¿Deseas continuar? (sí/no): " -r
echo ""

# Aceptar múltiples variaciones: sí, si, yes, y (con/sin tilde, mayúsculas)
if [[ ! $REPLY =~ ^([Ss]í?|[Yy]es?|[Yy])$ ]]; then
    echo -e "${YELLOW}Cancelado.${NC}"
    exit 0
fi

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[1/3]${NC} Borrando todas las tablas..."
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Función para ejecutar SQL
execute_sql() {
    local sql_file="$1"
    local step_name="$2"

    if command -v psql &> /dev/null; then
        # Local: usar psql directamente
        psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" < "$sql_file"
    elif command -v docker &> /dev/null && docker ps | grep -q familytrack-db; then
        # Docker: usar stdin con docker exec
        cat "$sql_file" | docker exec -i familytrack-db psql -U "$DB_USER" -d "$DB_NAME"
    else
        echo -e "${RED}✗ No se encontró psql ni Docker con familytrack-db${NC}"
        exit 1
    fi
}

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[1/3]${NC} Borrando todas las tablas..."
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if execute_sql "$SCRIPT_DIR/00-reset-db.sql" "reset"; then
    echo -e "${GREEN}✓ Tablas borradas exitosamente${NC}"
else
    echo -e "${RED}✗ Error al borrar tablas${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[2/3]${NC} Creando esquema v2.0..."
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if execute_sql "$SCRIPT_DIR/01-schema-v2.sql" "schema"; then
    echo -e "${GREEN}✓ Esquema v2.0 creado exitosamente${NC}"
else
    echo -e "${RED}✗ Error al crear esquema${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}[3/3]${NC} Insertando datos de ejemplo..."
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if execute_sql "$SCRIPT_DIR/02-seed-v2.sql" "seed"; then
    echo -e "${GREEN}✓ Datos de ejemplo insertados exitosamente${NC}"
else
    echo -e "${RED}✗ Error al insertar datos${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║           ✓ Base de datos lista para usar             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}Resumen de datos:${NC}"
echo "  📱 6 usuarios creados"
echo "  👨‍👩‍👧‍👦 1 familia con código: FAM123"
echo "  🏠 3 zonas seguras configuradas"
echo "  📍 6 ubicaciones simuladas en Madrid"
echo "  💬 6 mensajes de chat"
echo "  📸 2 fotos compartidas"
echo "  🚨 2 alertas de ejemplo"
echo ""

echo -e "${YELLOW}Próximos pasos:${NC}"
echo "  1. Reinicia n8n para que reconozca el nuevo estado de la BD"
echo "  2. Reinstala la app en el móvil"
echo "  3. La app ahora mostrará Onboarding en primer uso"
echo ""

