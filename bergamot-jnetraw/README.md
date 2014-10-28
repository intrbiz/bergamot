# Bergamot Raw Networking

This is a simple library to enable Java to utilise Linux Raw sockets, this 
allows a Java application to send and receive ICMP pings.

## CAP_NET_RAW

You will need to grant Java raw network capabilities to be able to use raw 
sockets without having to be root.

To do this, grant the cap_net_raw capability to the Java executable:

    setcap cap_net_raw+epi $JAVA_HOME/bin/java
  
Next ensure the libraries used by Java are trusted, add the following to `/etc/ld.so.conf.d/java.conf`:
  
    $JAVA_HOME/lib/
    $JAVA_HOME/lib/amd64/
    $JAVA_HOME/lib/amd64/jli/
    $JAVA_HOME/jre/lib/
    $JAVA_HOME/jre/lib/amd64/
    $JAVA_HOME/jre/lib/amd64/jli/
    $JAVA_HOME/jre/lib/amd64/server/

Replace `$JAVA_HOME` as appropriate.
