#!/bin/bash

cd event-bus-client
mvn clean install

cd ../event-bus-rabbit
mvn clean install

cd ../event-bus-toposervice
mvn clean install
