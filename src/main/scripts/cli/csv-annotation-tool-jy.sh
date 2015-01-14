#!/bin/bash

####
# OMERO CSV Annotation Tool for command line use via JVM + Jython script invocation.
# 
# Arguments:
#     csv-annotation-tool-jy.sh --help

# Lookup common dependencies set (assumed from current directory)
BIN_DIR=`dirname $0`
source "$BIN_DIR/dependencies.sh"

# Jython script from the current distribution
CSV_TOOL_JYTHON_SCRIPT="$BIN_DIR/csv-annotation-tool.jy"

# Extra Jython classpath
# Currrenly assumed to be present in the same directory as the CSV tool distribution
# Uses the Jython standalone distribution
JYTHON_VERSION="2.7-b3"
JYTHON_CLASSPATH=$JARS_DIR/jython-standalone-$JYTHON_VERSION.jar

# use both "standalone" distributions
RUN_CLASSPATH="$JYTHON_CLASSPATH:$STANDALONE_CLASSPATH"
MAIN_CLASS="org.python.util.jython"
JVM_OPTS="-Xms64M -Xmx256M"

java $JVM_OPTS -classpath $RUN_CLASSPATH $MAIN_CLASS $CSV_TOOL_JYTHON_SCRIPT $@
