FROM bergamot-base:latest
MAINTAINER Chris Ellis <chris@intrbiz.com>

# Install common nagios plugins
RUN zypper -q -n ref && zypper -q -n in monitoring-plugins-dummy monitoring-plugins-dbi monitoring-plugins-dbi-mysql monitoring-plugins-dbi-pgsql monitoring-plugins-dhcp monitoring-plugins-dig monitoring-plugins-disk_smb monitoring-plugins-dns monitoring-plugins-dns.pl monitoring-plugins-fping monitoring-plugins-haproxy monitoring-plugins-http monitoring-plugins-icmp monitoring-plugins-ldap monitoring-plugins-mysql monitoring-plugins-mysql_health monitoring-plugins-nrpe monitoring-plugins-ntp_peer monitoring-plugins-pgsql monitoring-plugins-ping monitoring-plugins-postgres monitoring-plugins-radius monitoring-plugins-rpc monitoring-plugins-rsync monitoring-plugins-sip monitoring-plugins-smtp monitoring-plugins-snmp monitoring-plugins-ssh monitoring-plugins-tcp monitoring-plugins-time

# Setup our directories
RUN mkdir -p /etc/bergamot/worker
RUN mkdir -p /opt/bergamot/worker
RUN mkdir -p /opt/bergamot/plugin/nagios

# Set our workdir
WORKDIR /opt/bergamot/worker

# Add our application
COPY bergamot-worker.app bergamot-worker.app

# Extract the application
RUN java -Dbootstrap.extract.only=true -jar bergamot-worker.app

# Inject the default configuration
COPY default.xml /etc/bergamot/worker/default.xml

# Expose our ports
EXPOSE 15080
EXPOSE 8161

# The run command
CMD ["java", "-Dbootstrap.extract=false", "-jar", "bergamot-worker.app"]
