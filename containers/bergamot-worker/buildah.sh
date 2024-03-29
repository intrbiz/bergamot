#!/bin/bash -ex
NAME=$1
TAG_SUFFIX=$2
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest${TAG_SUFFIX})
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 15080 --port 8161 --port 9002 --workingdir '/opt/bergamot/worker' --cmd '/entrypoint.sh' $ID

# Setup our directories
buildah run $ID mkdir -p /opt/bergamot/worker
buildah run $ID mkdir -p /opt/bergamot/plugins/nagios

# Install common nagios plugins
buildah run $ID zypper -q -n ref
buildah run $ID zypper -q -n in monitoring-plugins-dummy monitoring-plugins-dbi monitoring-plugins-dbi-mysql monitoring-plugins-dbi-pgsql monitoring-plugins-dhcp monitoring-plugins-dig monitoring-plugins-disk_smb monitoring-plugins-dns monitoring-plugins-dns.pl monitoring-plugins-fping monitoring-plugins-haproxy monitoring-plugins-http monitoring-plugins-icmp monitoring-plugins-ldap monitoring-plugins-mysql monitoring-plugins-mysql_health monitoring-plugins-nrpe monitoring-plugins-ntp_peer monitoring-plugins-pgsql monitoring-plugins-ping monitoring-plugins-postgres monitoring-plugins-radius monitoring-plugins-rpc monitoring-plugins-rsync monitoring-plugins-sip monitoring-plugins-smtp monitoring-plugins-snmp monitoring-plugins-ssh monitoring-plugins-tcp monitoring-plugins-time

# Add our application
buildah copy $ID ./bergamot-worker.app /opt/bergamot/worker/bergamot-worker.app
buildah copy $ID ./java.security /opt/bergamot/worker/java.security

# Copy the application entry point
buildah copy $ID ./entrypoint.sh /entrypoint.sh

# Extract the application
buildah run $ID sh -c 'cd /opt/bergamot/worker && java -Dbootstrap.extract.only=true -jar bergamot-worker.app'

# Make the image
buildah commit $ID $NAME
