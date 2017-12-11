set HOST=localhost
set PORT=9876
set CLIENT_POOL_SIZE=5
set JMX_PORT=9010

set SCRIPT=%0
set SCRIPTPATH=%~dp0

set CP=$(ls %SCRIPTPATH%ufc-load-testing-job-service-${project.version}.jar)
set MAIN=org.codingmatters.ufc.load.testing.service.JobServicesApp
set OPTS="--port %PORT% --host %HOST% --client-pool-size %CLIENT_POOL_SIZE%"

set JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=%JMX_PORT% -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

set JAVA_OPTS="-Xms128m -Xmx128m -Dlog-dir=%SCRIPTPATH%logs"

java -cp %CP% %JAVA_OPTS% %JMX_OPTS% %MAIN% %OPTS%