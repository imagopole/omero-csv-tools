#!/bin/bash

####
# OMERO CSV Annotation Tool for command line use via direct JVM invocation.
#
# Arguments:
#     csv-annotation-tool-cli.sh --help

# Lookup common dependencies set (assumed from current directory)
if [ -L "$0" ]
then
  TOOLS_SCRIPT=`readlink "$0"`
else
  TOOLS_SCRIPT=$0
fi

BIN_DIR=`dirname "$TOOLS_SCRIPT"`
source "$BIN_DIR/dependencies.sh"

# use the "standalone" distribution
RUN_CLASSPATH="$STANDALONE_CLASSPATH"
# uncomment to use the "with-dependencies" distribution instead
# RUN_CLASSPATH="$DEPENDENCIES_CLASSPATH"
MAIN_CLASS="org.imagopole.omero.tools.CsvAnnotatorCliMain"
JVM_OPTS="-Xms64M -Xmx256M"

java $JVM_OPTS -classpath $RUN_CLASSPATH $MAIN_CLASS $@
