#!/bin/bash

function build_util {
    NAME=$1
    TAG_SUFFIX=$(get_tag_suffix)
    echo "Building util container $NAME latest${TAG_SUFFIX}"
    cd $NAME
    ./buildah.sh "bergamotmonitoring/$NAME:latest${TAG_SUFFIX}" "${TAG_SUFFIX}"
    buildah push "bergamotmonitoring/$NAME:latest${TAG_SUFFIX}" "docker://docker.io/bergamotmonitoring/$NAME:latest${TAG_SUFFIX}"
    cd ..
}

function build_app {
    NAME=$1
    BERGAMOT_VERSION=$2
    TAG_SUFFIX=$(get_tag_suffix)
    echo "Building application container $NAME version ${BERGAMOT_VERSION}${TAG_SUFFIX}"
    cd $NAME
    ./buildah.sh "bergamotmonitoring/$NAME:${BERGAMOT_VERSION}${TAG_SUFFIX}" "${TAG_SUFFIX}"
    buildah push "bergamotmonitoring/$NAME:${BERGAMOT_VERSION}${TAG_SUFFIX}" "docker://docker.io/bergamotmonitoring/$NAME:${BERGAMOT_VERSION}${TAG_SUFFIX}"
    cd ..
}

function get_app_version {
    cd ..
    echo $(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    cd containers
}

function get_tag_suffix {
    ARCH=$(uname -m)
    if [ "$ARCH" = "x86_64" ]; then
        echo ""
    else
        echo "-$ARCH"
    fi
}
