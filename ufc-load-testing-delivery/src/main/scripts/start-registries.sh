#!/usr/bin/env bash

HOST=localhost
PORT=9876

SCRIPT="$(readlink --canonicalize-existing "$0")"
SCRIPTPATH="$(dirname "$SCRIPT")"

CP=$(ls $SCRIPTPATH/ufc-load-testing-job-service-*.jar)
MAIN=org.codingmatters.ufc.load.testing.service.JobServicesApp
OPTS="--port $PORT --host $HOST"

mkdir -p $SCRIPTPATH/logs
JAVA_OPTS="-Dlog-dir=$SCRIPTPATH/logs"

java -cp $CP $JAVA_OPTS $MAIN $OPTS