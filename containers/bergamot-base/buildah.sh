#!/bin/bash
NAME=$1
ID=$(buildah from opensuse/tumbleweed:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' $ID

# Install the base JDK
buildah run $ID zypper -q -n ref && zypper -q -n in java-11-openjdk-devel tar

# Setup some common directories
buildah run $ID mkdir -p /etc/bergamot
buildah run $ID mkdir -p /opt/bergamot

# Make the image
buildah commit $ID $NAME
