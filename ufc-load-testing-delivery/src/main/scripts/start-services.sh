#!/usr/bin/env bash

MEM=32m
EXPOSE_JMX_METRICS=true

HOST=localhost
PORT=9876
CLIENT_POOL_SIZE=5

CLEANUP_RATE=0
CLEANUP_KEPT=100000

JMX_PORT=9010

SCRIPT="$(readlink --canonicalize-existing "$0")"
SCRIPTPATH="$(dirname "$SCRIPT")"

CP=$(ls $SCRIPTPATH/ufc-load-testing-job-service-${project.version}.jar)
MAIN=org.codingmatters.ufc.load.testing.service.JobServicesApp
OPTS="--port $PORT --host $HOST --client-pool-size $CLIENT_POOL_SIZE"
OPTS="$OPTS  --cleanup.rate $CLEANUP_RATE --cleanup.kept $CLEANUP_KEPT"

JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMX_PORT"
JMX_OPTS="$JMX_OPTS -Dcom.sun.management.jmxremote.local.only=false"
JMX_OPTS="$JMX_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
JMX_OPTS="$JMX_OPTS -Dcom.sun.management.jmxremote.ssl=false"

JAVA_OPTS="-Xms$MEM -Xmx$MEM -Dlog-dir=$SCRIPTPATH/logs -Dexpose.jmx.jobs.metrics=$EXPOSE_JMX_METRICS"

java -cp $CP $JAVA_OPTS $JMX_OPTS $MAIN $OPTS