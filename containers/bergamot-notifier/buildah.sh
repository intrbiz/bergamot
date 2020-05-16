#!/bin/bash -ex
NAME=$1
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 15080 --port 8161 --workingdir '/opt/bergamot/notifier' --cmd '/usr/bin/java "-Dbootstrap.extract=false" "-jar" "bergamot-notifier.app"' $ID

# Setup our directories
buildah run $ID mkdir -p /opt/bergamot/notifier

# Add our application
buildah copy $ID ./bergamot-notifier.app /opt/bergamot/notifier/bergamot-notifier.app

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/notifier && java -Dbootstrap.extract.only=true -jar bergamot-notifier.app'

# Make the image
buildah commit $ID $NAME
