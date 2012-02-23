#!/bin/bash

CAN_DEPLOY=0
EVENT_BUS_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
EVENT_BUS_CLIENT_DIR="$EVENT_BUS_DIR/event-bus-client"
EVENT_BUS_RABBIT_DIR="$EVENT_BUS_DIR/event-bus-rabbit"
EVENT_BUS_TOPO_DIR="$EVENT_BUS_DIR/event-bus-toposervice"
EVENT_BUS_TOPO_CONFIG_DIR="$EVENT_BUS_DIR/event-bus-toposervice-config"
EVENT_BUS_ESP_DIR="$EVENT_BUS_DIR/services/event-stream-processor"
EVENT_BUS_ESP_CONFIG_DIR="$EVENT_BUS_DIR/services/event-stream-processor-config"
if [ -d "$EVENT_BUS_DIR/../felix-framework-4.0.2" ]; then
    FELIX_DIR="$( cd -P "$EVENT_BUS_DIR/../felix-framework-4.0.2" && pwd )"
    FELIX_BUNDLE_DIR="$FELIX_DIR/bundle"
    FELIX_CACHE_DIR="$FELIX_DIR/felix-cache"
    CAN_DEPLOY=1
fi

cd "$EVENT_BUS_CLIENT_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_RABBIT_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_TOPO_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_TOPO_CONFIG_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_ESP_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_ESP_CONFIG_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

if [ $CAN_DEPLOY -ne 1 ]; then
echo "not deploying"
    exit -1
fi

echo "deploying"

cp "$EVENT_BUS_CLIENT_DIR/target/"*.jar "$FELIX_BUNDLE_DIR/."
cp "$EVENT_BUS_RABBIT_DIR/target/"*.jar "$FELIX_BUNDLE_DIR/."
cp "$EVENT_BUS_TOPO_DIR/target/"*.jar "$FELIX_BUNDLE_DIR/."
cp "$EVENT_BUS_TOPO_CONFIG_DIR/target/"*.jar "$FELIX_BUNDLE_DIR/."
cp "$EVENT_BUS_ESP_DIR/target/"*.jar "$FELIX_BUNDLE_DIR/."
cp "$EVENT_BUS_ESP_CONFIG_DIR/target/"*.jar "$FELIX_BUNDLE_DIR/."

rm -Rf "$FELIX_CACHE_DIR"

cd "$FELIX_DIR"
java -jar bin/felix.jar
