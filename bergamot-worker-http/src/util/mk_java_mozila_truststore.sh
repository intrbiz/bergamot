#!/bin/sh
################################################################################
################################################################################
#                                                                              #
# Create a Java KeyStore from the Mozilla Trust Store                          #
#                                                                              #
# This script uses scripts from the CURL project and                           #
# from keyutil, thanks to those projects!                                      #
#                                                                              #
# Please see:                                                                  #
# * http://curl.haxx.se/                                                       #
# * https://code.google.com/p/java-keyutil/                                    #
#                                                                              #
################################################################################
################################################################################

# Fetch the latest CA Bundle
./mk-ca-bundle.pl

# Make the Java KeyStore

java -jar keyutil-0.4.0.jar --import --new-keystore trust_store.jks --password bergamot --import-pem-file ca-bundle.crt

