#!/bin/bash

EVENT_BUS_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
EVENT_BUS_CLIENT_DIR="$EVENT_BUS_DIR/event-bus-client"
EVENT_BUS_RABBIT_DIR="$EVENT_BUS_DIR/event-bus-rabbit"
EVENT_BUS_TOPO_DIR="$EVENT_BUS_DIR/event-bus-toposervice"
EVENT_BUS_TOPO_CONFIG_DIR="$EVENT_BUS_DIR/event-bus-toposervice-config"
EVENT_BUS_ESP_DIR="$EVENT_BUS_DIR/services/event-stream-processor"
EVENT_BUS_ESP_CONFIG_DIR="$EVENT_BUS_DIR/services/event-stream-processor-config"

JBOSS_HOME="$( cd -P $JBOSS_HOME && pwd )"
echo $JBOSS_HOME
JBOSS_DEPLOYMENT_DIR="$JBOSS_HOME/standalone/deployments"

rm "$JBOSS_DEPLOYMENT_DIR/"event-bus-*

cd "$EVENT_BUS_CLIENT_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_RABBIT_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_TOPO_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

cd "$EVENT_BUS_TOPO_CONFIG_DIR"
mvn clean -Dmaven.test.skip=true install bundle:bundle

#cd "$EVENT_BUS_ESP_DIR"
#mvn clean -Dmaven.test.skip=true install bundle:bundle

#cd "$EVENT_BUS_ESP_CONFIG_DIR"
#mvn clean -Dmaven.test.skip=true install bundle:bundle

cp "$EVENT_BUS_CLIENT_DIR/target/"*.jar "$JBOSS_DEPLOYMENT_DIR/."
cp "$EVENT_BUS_RABBIT_DIR/target/"*.jar "$JBOSS_DEPLOYMENT_DIR/."
cp "$EVENT_BUS_TOPO_DIR/target/"*.jar "$JBOSS_DEPLOYMENT_DIR/."
cp "$EVENT_BUS_TOPO_CONFIG_DIR/target/"*.jar "$JBOSS_DEPLOYMENT_DIR/."
#cp "$EVENT_BUS_ESP_DIR/target/"*.jar "$JBOSS_DEPLOYMENT_DIR/."
#cp "$EVENT_BUS_ESP_CONFIG_DIR/target/"*.jar "$JBOSS_DEPLOYMENT_DIR/."

