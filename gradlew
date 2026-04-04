#!/bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"

if [ ! -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  echo "gradle-wrapper.jar não encontrado. Gere o wrapper com 'gradle wrapper' ou baixe o projeto base pelo Spring Initializr." >&2
  exit 1
fi

exec "$JAVA_CMD" -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
