#!/usr/bin/env bash
# Crea la base deportal con las tablas y los datos de prueba.
# Desde la raíz del repositorio: ./database/create.sh

set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
env_file="$root/backend/.env"

if [[ ! -f "$env_file" ]]; then
  echo "Falta backend/.env. Cópialo desde backend/.env.example y completa el usuario y la contraseña de MySQL." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

if [[ -z "${DB_HOST:-}" || -z "${DB_PORT:-}" || -z "${SPRING_DATASOURCE_USERNAME:-}" || -z "${SPRING_DATASOURCE_PASSWORD:-}" ]]; then
  echo "backend/.env necesita DB_HOST, DB_PORT, SPRING_DATASOURCE_USERNAME y SPRING_DATASOURCE_PASSWORD." >&2
  exit 1
fi

defaults="$(mktemp)"
trap 'rm -f "$defaults"' EXIT
cat > "$defaults" <<EOF
[client]
host=$DB_HOST
port=$DB_PORT
user=$SPRING_DATASOURCE_USERNAME
password=$SPRING_DATASOURCE_PASSWORD
EOF

mysql --defaults-extra-file="$defaults" < "$root/database/deportal.sql"
echo "Base deportal lista."
