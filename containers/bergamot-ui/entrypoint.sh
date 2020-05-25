#!/bin/bash

JMX_PASSWORD="${JMX_PASSWORD:-monitor}"

echo "monitor $JMX_PASSWORD" > /opt/bergamot/ui/jmx_pass
chmod 600 /opt/bergamot/ui/jmx_pass
echo "monitor readonly" > /opt/bergamot/ui/jmx_access

JMX_PORT="${JMX_PORT:-9001}"
JMX_OPTS="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.rmi.port=$JMX_PORT -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.password.file=/opt/bergamot/ui/jmx_pass -Dcom.sun.management.jmxremote.access.file=/opt/bergamot/ui/jmx_access"

exec /usr/bin/java $JVM_OPTS $JMX_OPTS "-Dbootstrap.extract=false" "-Dbalsa.env=prod" "-jar" "bergamot-ui.app"
