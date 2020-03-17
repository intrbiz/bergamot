#!/bin/bash

function docker_build_util {
    NAME=$1
    echo "Building util container $NAME"
    cd $NAME
    docker build --no-cache $DOCKER_OPTS -t $NAME:latest -t bergamotmonitoring/$NAME:latest .
    docker push bergamotmonitoring/$NAME:latest
    cd ..
}

function docker_build_app {
    NAME=$1
    BERGAMOT_VERSION=$2
    echo "Building application container $NAME version $BERGAMOT_VERSION"
    cd $NAME
    docker build --no-cache $DOCKER_OPTS --build-arg bergamot_version=$BERGAMOT_VERSION -t $NAME:$BERGAMOT_VERSION -t bergamotmonitoring/$NAME:$BERGAMOT_VERSION .
    docker push bergamotmonitoring/$NAME:$BERGAMOT_VERSION
    cd ..
}

function get_app_version {
    cd ..
    echo $(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    cd containers
}
