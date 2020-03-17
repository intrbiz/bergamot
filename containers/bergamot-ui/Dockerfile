FROM bergamot-base:latest
MAINTAINER Chris Ellis <chris@intrbiz.com>

# Build arguments
ARG bergamot_version=2.0.0

# Setup our directories
RUN mkdir -p /etc/bergamot/ui
RUN mkdir -p /etc/bergamot/config
RUN mkdir -p /opt/bergamot/ui

# Set our workdir
WORKDIR /opt/bergamot/ui

# Expose ports
EXPOSE 5701
EXPOSE 8090
EXPOSE 8081

# Add our application
COPY bergamot-ui.app bergamot-ui.app

# Extract the application
RUN java -Dbootstrap.extract.only=true -jar bergamot-ui.app

# Inject the default configuration
COPY default.xml /etc/bergamot/ui/default.xml
COPY hazelcast.xml /etc/bergamot/ui/hazelcast.xml

# Add the default site configuration templates
COPY bergamot-site-config-template.tar.gz /etc/bergamot/config/bergamot-site-config-template.tar.gz
RUN cd /etc/bergamot/config && tar -xzf bergamot-site-config-template.tar.gz && rm -f bergamot-site-config-template.tar.gz

# The run command
CMD ["java", "-Dbootstrap.extract=false", "-Dbalsa.env=prod", "-jar", "bergamot-ui.app"]
