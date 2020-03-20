FROM opensuse:42.2
MAINTAINER Chris Ellis <chris.ellis@hivehome.com>

# Install OpenSSH
RUN zypper -q -n in openssh

# Copy our SFTP only SSHD config file
COPY sshd_config /etc/ssh/sshd_config

# Copy our entrypoint
COPY entrypoint.sh /entrypoint.sh

# Our incomming directory
RUN mkdir /incoming
RUN chown root.root /incoming && chmod 755 /incoming

# Data directory
RUN mkdir /data
RUN chown root.root /data && chmod 755 /data

# Create SFTP group
RUN groupadd -g 1180 sftp

# Create BG Data user
RUN useradd -u 1190 -g sftp -m -d /data/bgdata -s /bin/false bgdata
RUN mkdir /data/bgdata/.ssh
RUN chown bgdata.sftp /data/bgdata/.ssh && chmod 700 /data/bgdata/.ssh
COPY bgdata_authorized_keys /data/bgdata/.ssh/authorized_keys
RUN chown bgdata.sftp /data/bgdata/.ssh/authorized_keys && chmod 600 /data/bgdata/.ssh/authorized_keys

# Create demandresponse user
RUN useradd -u 1191 -g sftp -m -d /data/demandresponse -s /bin/false demandresponse
RUN mkdir /data/demandresponse/.ssh
RUN chown demandresponse.sftp /data/demandresponse/.ssh && chmod 700 /data/demandresponse/.ssh
COPY demandresponse_authorized_keys /data/demandresponse/.ssh/authorized_keys
RUN chown demandresponse.sftp /data/demandresponse/.ssh/authorized_keys && chmod 600 /data/demandresponse/.ssh/authorized_keys

# Expose the port
EXPOSE 2222

# The command
CMD [ "/entrypoint.sh" ]
