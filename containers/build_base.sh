#!/bin/bash

. ./build_functions.sh

docker_build_util bergamot-base

docker_build_util bergamot-build
