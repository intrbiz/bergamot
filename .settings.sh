!#/bin/sh

cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>intrbiz</id>
      <username>cellis</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>snapshots</id>
      <username>cellis</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF
