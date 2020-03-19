#!/bin/bash
NAME=$1
ID=$(buildah from bergamotmonitoring/bergamot-base:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 8080 --user nginx --workingdir '/opt/bergamot/ui' --cmd '[ "/usr/sbin/nginx", "-g", "daemon off;", "-c", "/etc/nginx/nginx.conf" ]' $ID

# Install nginx
buildah run $ID zypper -q -n ref && zypper -q -n in nginx

# Pull in the public resources from the UI
buildah run $ID mkdir -p /opt/bergamot/ui

# Add our application
buildah copy $ID ./bergamot-ui.app /opt/bergamot/ui/bergamot-ui.app

# Extract the application
buildah run $ID cd /opt/bergamot/ui/ && java -Dbootstrap.extract.only=true -jar bergamot-ui.app

# Pid dir
buildah run $ID mkdir -p /run/nginx && chown nginx /run/nginx

# Custom conf dir
buildah run $ID mkdir -p /etc/nginx/conf.d && chown nginx /etc/nginx/conf.d

# Copy in our configuration
buildah copy $ID ./nginx.conf /etc/nginx/nginx.conf
buildah copy $ID ./balsa_scgi_params.conf /etc/nginx/balsa_scgi_params.conf
buildah copy $ID ./upstream.conf /etc/nginx/conf.d/upstream.conf

# Make the image
buildah commit $ID $NAME
