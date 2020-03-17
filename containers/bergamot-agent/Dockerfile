FROM bergamot-base:latest
MAINTAINER Chris Ellis <chris@intrbiz.com>

# Setup our directories
RUN mkdir -p /opt/bergamot/agent

# Set our workdir
WORKDIR /opt/bergamot/agent

# Add our application
COPY bergamot-agent.app bergamot-agent.app
COPY libsigar-amd64-linux.so /usr/lib64/

# Extract the application
RUN java -Dbootstrap.extract.only=true -jar bergamot-agent.app

# The run command
CMD ["java", "-Dbootstrap.extract=false", "-jar", "bergamot-agent.app"]
