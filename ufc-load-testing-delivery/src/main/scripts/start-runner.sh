#!/usr/bin/env bash

RUNNER_NAME="forrest-gump"

REGISTRY_URL=http://localhost:9876
HOST=localhost
PORT=9711

MIN_PROCESS_TIME=5000
MAX_PROCESS_TIME=30000

SCRIPT="$(readlink --canonicalize-existing "$0")"
SCRIPTPATH="$(dirname "$SCRIPT")"

CP=$(ls $SCRIPTPATH/ufc-load-testing-job-runner-*.jar)
MAIN=org.codingmatters.ufc.load.testing.runner.JobRunnerApp
OPTS="--registry $REGISTRY_URL --port $PORT --host $HOST --min-process-time $MIN_PROCESS_TIME --max-process-time $MAX_PROCESS_TIME"

mkdir -p $SCRIPTPATH/logs
JAVA_OPTS="-Dlog-dir=$SCRIPTPATH/logs -Drunner-name=$RUNNER_NAME"

java -cp $CP $JAVA_OPTS $MAIN $OPTS