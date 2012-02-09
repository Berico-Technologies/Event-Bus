#!/bin/bash

cd event-bus-client
mvn clean -Dmaven.test.skip=true install

cd ../event-bus-rabbit
mvn clean -Dmaven.test.skip=true install

cd ../event-bus-toposervice
mvn clean -Dmaven.test.skip=true install
