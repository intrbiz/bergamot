#!/bin/bash

set -ex

. ./build_functions.sh

docker_build_util bergamot-base

docker_build_util bergamot-build
