#!/usr/bin/env bash

HOST=localhost
PORT=9876

SCRIPT="$(readlink --canonicalize-existing "$0")"
SCRIPTPATH="$(dirname "$SCRIPT")"

CP=$(ls $SCRIPTPATH/ufc-load-testing-job-service-*.jar)
MAIN=org.codingmatters.ufc.load.testing.service.JobServicesApp
OPTS="--port $PORT --host $HOST --client-pool-size 5"

mkdir -p $SCRIPTPATH/logs
JAVA_OPTS="-Xms128m -Xmx128m -Dlog-dir=$SCRIPTPATH/logs"

java -cp $CP $JAVA_OPTS $MAIN $OPTS