#!/bin/sh

path=$CERT_PATH/*
for entry in $path
do
  if [ -f "$entry" ];then
    aliasName=$(echo $(echo $entry | cut -d '/' -f 5) | cut -d '.' -f 1)
    keytool -import -trustcacerts -keystore\
    /usr/lib/jvm/java-11-openjdk/jre/lib/security/cacerts\
    -storepass changeit -noprompt -alias \
    "$aliasName" -file "$entry"
    echo "installed cert : $aliasName"
  fi
done


java $BOOTAPP_OPTS $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar /virtual-accounts.jar