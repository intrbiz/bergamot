#!/bin/bash

set -ex

. ./build_functions.sh

BERGAMOT_VERSION=$(get_app_version)

echo "Building containers for Bergamot $BERGAMOT_VERSION"

# Copy over artifacts
cp ../bergamot-worker/target/bergamot-worker-$BERGAMOT_VERSION.app ./bergamot-worker/bergamot-worker.app
cp ../bergamot-notifier/target/bergamot-notifier-$BERGAMOT_VERSION.app ./bergamot-notifier/bergamot-notifier.app
cp ../bergamot-ui/target/bergamot-ui-$BERGAMOT_VERSION.app ./bergamot-ui/bergamot-ui.app
cp ../bergamot-ui/target/bergamot-ui-$BERGAMOT_VERSION.app ./bergamot-ui-nginx/bergamot-ui.app
cp ../bergamot-agent/target/bergamot-agent-$BERGAMOT_VERSION.app ./bergamot-agent/bergamot-agent.app

# Build the template configuration
pushd ../bergamot-ui/src/main/cfg/template
tar -czvf bergamot-site-config-template.tar.gz *
popd
cp ../bergamot-ui/src/main/cfg/template/bergamot-site-config-template.tar.gz ./bergamot-ui/bergamot-site-config-template.tar.gz

# Build containers
build_app bergamot-agent $BERGAMOT_VERSION

build_app bergamot-worker $BERGAMOT_VERSION
build_app bergamot-notifier $BERGAMOT_VERSION

build_app bergamot-ui $BERGAMOT_VERSION
build_app bergamot-ui-nginx $BERGAMOT_VERSION
