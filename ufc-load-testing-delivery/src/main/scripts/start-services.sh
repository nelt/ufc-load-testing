#!/usr/bin/env bash

MEM=32m
EXPOSE_JMX_METRICS=false

HOST=localhost
PORT=9876
CLIENT_POOL_SIZE=5
JMX_PORT=9010

SCRIPT="$(readlink --canonicalize-existing "$0")"
SCRIPTPATH="$(dirname "$SCRIPT")"

CP=$(ls $SCRIPTPATH/ufc-load-testing-job-service-${project.version}.jar)
MAIN=org.codingmatters.ufc.load.testing.service.JobServicesApp
OPTS="--port $PORT --host $HOST --client-pool-size $CLIENT_POOL_SIZE"

JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

JAVA_OPTS="-Xms$MEM -Xmx$MEM -Dlog-dir=$SCRIPTPATH/logs -Dexpose.jmx.jobs.metrics=$EXPOSE_JMX_METRICS"

java -cp $CP $JAVA_OPTS $JMX_OPTS $MAIN $OPTS