#!/bin/bash

cd event-bus-client
mvn clean bundle:bundle

cd ../event-bus-rabbit
mvn clean bundle:bundle

cd ../event-bus-toposervice
mvn clean bundle:bundle
