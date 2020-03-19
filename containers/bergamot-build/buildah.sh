#!/bin/bash
NAME=$1
ID=$(buildah from bergamotmonitoring/bergamot-base:latest)
buildah config --author='Chris Ellis <chris@intrbiz.com>' $ID

# Install build tools
buildah run $ID zypper -q -n ref && zypper -q -n in maven buildah

# Install utils needed for unit test
buildah run $ID zypper -q -n ref && zypper -q -n in monitoring-plugins-dummy

# Make the image
buildah commit $ID $NAME
