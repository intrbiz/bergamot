#!/bin/bash -ex
NAME=$1
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 14080 --workingdir '/opt/bergamot/proxy' --cmd '/usr/bin/java "-Dbootstrap.extract=false" "-jar" "bergamot-proxy.app"' $ID

# Setup our directories
buildah run $ID mkdir -p /etc/bergamot/proxy
buildah run $ID mkdir -p /opt/bergamot/proxy

# Add our application
buildah copy $ID ./bergamot-proxy.app /opt/bergamot/proxy/bergamot-proxy.app
buildah copy $ID ./default.xml /etc/bergamot/proxy/default.xml

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/proxy && java -Dbootstrap.extract.only=true -jar bergamot-proxy.app'

# Make the image
buildah commit $ID $NAME
