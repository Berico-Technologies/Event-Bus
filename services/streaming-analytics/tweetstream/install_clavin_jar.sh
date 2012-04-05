#!/bin/bash
mvn install:install-file -Dfile=`pwd`/Clavin-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DgroupId=dodiis \
    -DartifactId=clavin -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar-with-dependencies
cp Clavin-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/.m2/repository/dodiis/clavin/0.0.1-SNAPSHOT/Clavin-0.0.1-SNAPSHOT-jar-with-dependencies.jar