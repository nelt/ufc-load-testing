#!/usr/bin/env bash

RUNNER_COUNT=1
REGISTRY_URL=http://localhost:9876
HOST=localhost
PORT_BASE=9700

MIN_PROCESS_TIME=1000
MAX_PROCESS_TIME=5000

SCRIPT="$(readlink --canonicalize-existing "$0")"
SCRIPTPATH="$(dirname "$SCRIPT")"

CP=$(ls $SCRIPTPATH/ufc-load-testing-job-runner-${project.version}.jar)
MAIN=org.codingmatters.ufc.load.testing.runner.JobRunnerApp
OPTS="--runner-count $RUNNER_COUNT --registry $REGISTRY_URL --port-base $PORT_BASE --host $HOST --min-process-time $MIN_PROCESS_TIME --max-process-time $MAX_PROCESS_TIME"

JAVA_OPTS="-Xms128m -Xmx128m -Dlog-dir=$SCRIPTPATH/logs "

java -cp $CP $JAVA_OPTS $MAIN $OPTS
