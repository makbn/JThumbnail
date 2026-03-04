#!/usr/bin/env bash
cd "$(dirname "$0")"
exec ./gradlew runMcpServer --console=plain
