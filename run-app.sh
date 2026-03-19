#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mvn -q -DskipTests package
java -jar target/terminal_text_buffer-1.0-SNAPSHOT.jar

