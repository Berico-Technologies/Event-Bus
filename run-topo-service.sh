#!/bin/bash

cd event-bus-toposervice
mvn exec:java -Dexec.mainClass="pegasus.eventbus.topology.service.TopologyService"
