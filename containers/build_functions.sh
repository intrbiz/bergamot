#!/bin/bash

function docker_build_util {
    NAME=$1
    echo "Building util container $NAME"
    pushd $NAME >/dev/null
    docker build --no-cache $DOCKER_OPTS -t $NAME:latest -t bergamotmonitoring/$NAME:latest .
    docker push bergamotmonitoring/$NAME:latest
    popd >/dev/null
}

function docker_build_app {
    NAME=$1
    BERGAMOT_VERSION=$2
    echo "Building application container $NAME version $BERGAMOT_VERSION"
    pushd $NAME >/dev/null
    docker build --no-cache $DOCKER_OPTS --build-arg bergamot_version=$BERGAMOT_VERSION -t $NAME:$BERGAMOT_VERSION -t bergamotmonitoring/$NAME:$BERGAMOT_VERSION .
    docker push bergamotmonitoring/$NAME:$BERGAMOT_VERSION
    popd >/dev/null
}

function get_app_version {
    pushd .. >/dev/null
    echo $(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    popd >/dev/null
}
