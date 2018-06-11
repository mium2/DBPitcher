#!/bin/sh
#
#
#
echo "starting db-pitcher-ee-1.0.0"
JAVA_HOME=/usr
CLASSPATH=.:./conf
#${JAVA_HOME}/bin/java -classpath $CLASSPATH -jar target/db-pushsms-pitcher-1.0.0.jar
nohup ${JAVA_HOME}/bin/java -XX:+UseParallelGC -Xms1g -Xmx2g  -classpath $CLASSPATH -jar target/db-pitcher-custom-1.0.0.jar &
