#!/bin/bash -ex
NAME=$1
TAG_SUFFIX=$2
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest${TAG_SUFFIX})
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 8080 --workingdir '/opt/bergamot/ui' --cmd '/usr/sbin/nginx "-g" "daemon off;" "-c" "/etc/nginx/nginx.conf"' $ID

# Install nginx
buildah run $ID zypper -q -n ref 
buildah run $ID zypper -q -n in nginx

# Pull in the public resources from the UI
buildah run $ID mkdir -p /opt/bergamot/ui

# Add our application
buildah copy $ID ./bergamot-ui.app /opt/bergamot/ui/bergamot-ui.app

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/ui/ && java -Dbootstrap.extract.only=true -jar bergamot-ui.app'

# Pid dir
buildah run $ID mkdir -p /run/nginx
buildah run $ID chown nginx /run/nginx

# Custom conf dir
buildah run $ID mkdir -p /etc/nginx/conf.d
buildah run $ID chown nginx /etc/nginx/conf.d

# Copy in our configuration
buildah copy $ID ./nginx.conf /etc/nginx/nginx.conf
buildah copy $ID ./balsa_scgi_params.conf /etc/nginx/balsa_scgi_params.conf
buildah copy $ID ./upstream.conf /etc/nginx/conf.d/upstream.conf

# Set the user the container should run as
buildah config --user nginx $ID

# Make the image
buildah commit $ID $NAME
