#!/bin/bash -ex
NAME=$1
TAG_SUFFIX=$2
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest${TAG_SUFFIX})
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 14080 --port 9003 --workingdir '/opt/bergamot/proxy' --cmd '/entrypoint.sh' $ID

# Setup our directories
buildah run $ID mkdir -p /opt/bergamot/proxy

# Add our application
buildah copy $ID ./bergamot-proxy.app /opt/bergamot/proxy/bergamot-proxy.app

# Copy the application entry point
buildah copy $ID ./entrypoint.sh /entrypoint.sh

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/proxy && java -Dbootstrap.extract.only=true -jar bergamot-proxy.app'

# Make the image
buildah commit $ID $NAME
