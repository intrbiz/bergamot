FROM bergamotmonitoring/bergamot-base:latest
MAINTAINER Chris Ellis <chris@intrbiz.com>

# Install stuff needed for build
RUN zypper -q -n ref && zypper -q -n in docker maven

# Install stuff needed for unit tests
RUN zypper -q -n ref && zypper -q -n in monitoring-plugins-dummy
