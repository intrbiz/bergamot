#!/bin/bash -ex
NAME=$1
TAG_SUFFIX=$2
ID=$(buildah from docker.io/opensuse/tumbleweed:latest${TAG_SUFFIX})
buildah config --author='Chris Ellis <chris@intrbiz.com>' --port 2222 --entrypoint '/entrypoint.sh' $ID

# Install SSH and some other stuff
buildah run $ID zypper -q -n ref
buildah run $ID zypper -q -n in openssh shadow monitoring-plugins-dummy

# Copy our SFTP only SSHD config file
buildah copy $ID ./sshd_config /etc/ssh/sshd_config

# Copy our entrypoint
buildah copy $ID ./entrypoint.sh /entrypoint.sh
buildah run $ID chmod 755 /entrypoint.sh

# Create user accounts
# Password: abc123
buildah run $ID useradd -m -p '$6$YA74xneLzaOxcFAC$ESv/ClaEulXH9B4qqaHh9ocxkOeUEZogztmzgXIB1xTAAD9AWy/diR6TLxj7K/ZNeQpMRaUNBX4Q2c5164ZUZ1' passwduser
buildah run $ID useradd -m keyuser

buildah run $ID mkdir /home/keyuser/.ssh
buildah run $ID sh -c 'chown keyuser.users /home/keyuser/.ssh && chmod 700 /home/keyuser/.ssh'

buildah copy $ID ./authorized_keys /home/keyuser/.ssh/authorized_keys
buildah copy $ID ./test_key /home/keyuser/.ssh/test_key
buildah copy $ID ./test_key.pub /home/keyuser/.ssh/test_key.pub
buildah run $ID sh -c 'chown keyuser.users /home/keyuser/.ssh/* && chmod 600 /home/keyuser/.ssh/*'

# Make the image
buildah commit $ID $NAME
