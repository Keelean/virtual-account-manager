#!/bin/sh
java -version
java $JVM_OPTS $BOOTAPP_OPTS -Djava.security.egd=file:/dev/./urandom -jar /virtual-accounts.jar
#java -Dhttps.protocols=SSLv3 $JVM_OPTS $BOOTAPP_OPTS -Djava.security.egd=file:/dev/./urandom -jar /customer-accounts.jar