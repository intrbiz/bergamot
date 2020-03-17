FROM bergamot-base:latest
MAINTAINER Chris Ellis <chris@intrbiz.com>

# Setup our directories
RUN mkdir -p /etc/bergamot/notifier
RUN mkdir -p /opt/bergamot/notifier

# Set our workdir
WORKDIR /opt/bergamot/notifier

# Add our application
COPY bergamot-notifier.app bergamot-notifier.app

# Extract the application
RUN java -Dbootstrap.extract.only=true -jar bergamot-notifier.app

# Inject the default configuration
COPY default.xml /etc/bergamot/notifier/default.xml

# The run command
CMD ["java", "-Dbootstrap.extract=false", "-jar", "bergamot-notifier.app"]
