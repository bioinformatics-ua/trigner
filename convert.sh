#!/bin/bash
cp=target/trigner-1.0-SNAPSHOT-jar-with-dependencies.jar:$CLASSPATH
JAVA_COMMAND="java -Xmx4096m -Xms1024m -XX:MaxPermSize=256M -Xss128m -Djava.awt.headless=true -server -Dfile.encoding=UTF-8 -classpath $cp"
CLASS=pt.ua.tm.trigner.cli.Convert

$JAVA_COMMAND $CLASS $*

