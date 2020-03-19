#!/bin/bash

function build_util {
    NAME=$1
    echo "Building util container $NAME"
    cd $NAME
    ./buildah.sh "bergamotmonitoring/$NAME:latest"
    buildah push "bergamotmonitoring/$NAME:latest" "docker://docker.io/bergamotmonitoring/$NAME:latest"
    cd ..
}

function build_app {
    NAME=$1
    BERGAMOT_VERSION=$2
    echo "Building application container $NAME version $BERGAMOT_VERSION"
    cd $NAME
    ./buildah.sh "bergamotmonitoring/$NAME:$BERGAMOT_VERSION"
    buildah push "bergamotmonitoring/$NAME:$BERGAMOT_VERSION" "docker://docker.io/bergamotmonitoring/$NAME:$BERGAMOT_VERSION"
    cd ..
}

function get_app_version {
    cd ..
    echo $(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    cd containers
}
