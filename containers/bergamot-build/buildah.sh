#!/bin/bash -ex
NAME=$1
TAG_SUFFIX=$2
ID=$(buildah from docker.io/bergamotmonitoring/bergamot-base:latest${TAG_SUFFIX})
buildah config --author='Chris Ellis <chris@intrbiz.com>' $ID

# Install build tools
buildah run $ID zypper -q -n ref
buildah run $ID zypper -q -n in maven git

# Install utils needed for unit test
buildah run $ID zypper -q -n in monitoring-plugins-dummy

# Make the image
buildah commit $ID $NAME
