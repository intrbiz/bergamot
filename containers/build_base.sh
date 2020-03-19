#!/bin/bash

set -ex

. ./build_functions.sh

build_util bergamot-base

build_util bergamot-build
