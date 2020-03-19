#!/bin/bash -ex
NAME=$1
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' --workingdir '/opt/bergamot/agent' --cmd '["java", "-Dbootstrap.extract=false", "-jar", "bergamot-agent.app"]' $ID

# Setup our directories
buildah run $ID mkdir -p /opt/bergamot/agent

# Add our application
buildah copy $ID ./bergamot-agent.app /opt/bergamot/agent/bergamot-agent.app
buildah copy $ID ./libsigar-amd64-linux.so /usr/lib64/libsigar-amd64-linux.so

# Extract the application
buildah run $ID cd /opt/bergamot/agent && java -Dbootstrap.extract.only=true -jar bergamot-agent.app

# Make the image
buildah commit $ID $NAME
