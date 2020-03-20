#!/bin/bash

set -ex

. ./build_functions.sh

# Base container
build_util bergamot-base

# Build container
build_util bergamot-build

# Test containers
build_util bergamot-test-ssh
