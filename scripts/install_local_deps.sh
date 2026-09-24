#!/usr/bin/env bash
# Installs the Forge 1.20.1 builds of Veil and Sable (which aren't on a public maven yet) into Maven Local, so the
# Gradle build can resolve them. The sable-companion jar is taken from inside the Sable jar. The coordinates match what
# `./gradlew publishToMavenLocal` in the Veil and Sable repositories installs, so building those from source works too.
#
#   scripts/install_local_deps.sh <veil-forge-1.20.1-X.jar> <sable-forge-1.20.1-Y.jar>
set -euo pipefail

VEIL_JAR=$1
SABLE_JAR=$2
M2=${MAVEN_LOCAL:-$HOME/.m2/repository}

install() { # group artifact version jar
    local dir="$M2/${1//.//}/$2/$3"
    mkdir -p "$dir"
    cp "$4" "$dir/$2-$3.jar"
    cat > "$dir/$2-$3.pom" <<POM
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>$1</groupId>
  <artifactId>$2</artifactId>
  <version>$3</version>
</project>
POM
    echo "installed $1:$2:$3"
}

version_of() { # jar prefix
    basename "$1" .jar | sed -E "s/.*$2-//"
}

VEIL_VERSION=$(version_of "$VEIL_JAR" "veil-forge-1.20.1")
SABLE_VERSION=$(version_of "$SABLE_JAR" "sable-forge-1.20.1")

install foundry.veil veil-forge-1.20.1 "$VEIL_VERSION" "$VEIL_JAR"
install dev.ryanhcode.sable sable-forge-1.20.1 "$SABLE_VERSION" "$SABLE_JAR"

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
unzip -q -o "$SABLE_JAR" 'META-INF/jarjar/*sable-companion*.jar' -d "$TMP"
COMPANION_JAR=$(ls "$TMP"/META-INF/jarjar/*sable-companion*.jar)
COMPANION_VERSION=$(basename "$COMPANION_JAR" .jar | sed -E 's/.*sable-companion-1\.20\.1-//')
install dev.ryanhcode.sable sable-sable_companion-1.20.1 "$COMPANION_VERSION" "$COMPANION_JAR"
