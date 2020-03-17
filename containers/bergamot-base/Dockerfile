FROM opensuse/tumbleweed:latest
MAINTAINER Chris Ellis <chris@intrbiz.com>

# Install the base JDK
RUN zypper -q -n ref && zypper -q -n in java-11-openjdk-devel tar

# Setup some common directories
RUN mkdir -p /etc/bergamot
RUN mkdir -p /opt/bergamot
