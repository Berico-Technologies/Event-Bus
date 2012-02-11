#!/bin/bash

cd event-bus-client
mvn clean -Dmaven.test.skip=true install

cd ../event-bus-rabbit
mvn clean -Dmaven.test.skip=true install bundle:bundle
cp target/*.jar ../../felix-framework-4.0.2/bundle/.

cd ../event-bus-toposervice
mvn clean -Dmaven.test.skip=true install bundle:bundle
cp target/*.jar ../../felix-framework-4.0.2/bundle/.

rm -rf ../../felix-framework-4.0.2/felix-cache
