#!/bin/bash -ex
NAME=$1
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 5701 --port 8090 --port 8081 --workingdir '/opt/bergamot/ui' --entrypoint '[ "/usr/bin/java" ]' --cmd '[ "-Dbootstrap.extract=false", "-jar", "bergamot-ui.app" ]' $ID

# Setup our directories
buildah run $ID mkdir -p /etc/bergamot/ui
buildah run $ID mkdir -p /etc/bergamot/config
buildah run $ID mkdir -p /opt/bergamot/ui

# Add our application
buildah copy $ID ./bergamot-ui.app /opt/bergamot/ui/bergamot-ui.app
buildah copy $ID ./default.xml /etc/bergamot/ui/default.xml
buildah copy $ID ./hazelcast.xml /etc/bergamot/ui/hazelcast.xml
buildah copy $ID ./bergamot-site-config-template.tar.gz /etc/bergamot/config/bergamot-site-config-template.tar.gz

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/ui && java -Dbootstrap.extract.only=true -jar bergamot-ui.app'

# Add the default site configuration templates
buildah run $ID sh -c 'cd /etc/bergamot/config && tar -xzf bergamot-site-config-template.tar.gz'

# Make the image
buildah commit $ID $NAME
