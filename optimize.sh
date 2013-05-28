#!/bin/bash
cp=target/trigner-1.0-SNAPSHOT-jar-with-dependencies.jar:$CLASSPATH
JAVA_COMMAND="java -Xmx4096m -Xms1024m -XX:MaxPermSize=256M -Xss128m -Djava.awt.headless=true -server -Dfile.encoding=UTF-8 -classpath $cp"

CMD=$1
shift

help() {
cat << EOF
Trigner optimize commands: 
	event 		optimize one model for one event trigger

Include -h or --help with any option for more information
EOF
echo $HELP
}

CLASS=

case $CMD in
	event)
		CLASS=pt.ua.tm.trigner.cli.optimize.Event;;
	*)
		echo "Unrecognized command: $CMD"; help; exit 1;;
esac

$JAVA_COMMAND $CLASS $*

