set RUNNER_COUNT=1
set REGISTRY_URL=http://localhost:9876
set HOST=localhost
set PORT_BASE=9700

set MIN_PROCESS_TIME=1000
set MAX_PROCESS_TIME=5000

set SCRIPT=%0
set SCRIPTPATH=%~dp0

set CP=$(ls %SCRIPTPATH%ufc-load-testing-job-runner-${project.version}.jar)
set MAIN=org.codingmatters.ufc.load.testing.runner.JobRunnerApp
set OPTS="--runner-count %RUNNER_COUNT% --registry %REGISTRY_URL% --port-base %PORT_BASE% --host %HOST% --min-process-time %MIN_PROCESS_TIME% --max-process-time %MAX_PROCESS_TIME%"

set JAVA_OPTS="-Xms128m -Xmx128m -Dlog-dir=%SCRIPTPATH%logs "

java -cp $CP $JAVA_OPTS $MAIN $OPTS
