#!/bin/bash -ex
NAME=$1
TAG_SUFFIX=$2
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest${TAG_SUFFIX})
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 9004 --workingdir '/opt/bergamot/notifier' --cmd '/entrypoint.sh' $ID

# Setup our directories
buildah run $ID mkdir -p /opt/bergamot/notifier

# Add our application
buildah copy $ID ./bergamot-notifier.app /opt/bergamot/notifier/bergamot-notifier.app

# Copy the application entry point
buildah copy $ID ./entrypoint.sh /entrypoint.sh

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/notifier && java -Dbootstrap.extract.only=true -jar bergamot-notifier.app'

# Make the image
buildah commit $ID $NAME
