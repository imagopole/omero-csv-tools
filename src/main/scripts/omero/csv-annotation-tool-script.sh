#!/bin/sh

####
# OMERO CSV Annotation Tool for use via OMERO.scripts invocation.
#
# Should be exposed on the PATH to be invoked by the 
# Csv_Annotation_Tool.py OMERO server-side script.
# 
# Arguments:
#     csv-annotation-tool-script.sh --help
#
# Note: similar to csv-annotation-tool-cli.sh, but with a 
# specific main class suited to the OMERO.scripts environment.

# Note: Jar dependencies otherwise sourced from "dependencies.sh" are inlined here
JARS_DIR=`dirname $0`
CSV_TOOL_VERSION="0.2-SNAPSHOT"
STANDALONE_CLASSPATH="$JARS_DIR/omero-csv-tools-$CSV_TOOL_VERSION-standalone.jar"

# use the "standalone" distribution
RUN_CLASSPATH="$STANDALONE_CLASSPATH"
MAIN_CLASS="org.imagopole.omero.tools.CsvAnnotatorScriptMain"
JVM_OPTS="-Xms64M -Xmx256M"

java $JVM_OPTS -classpath $RUN_CLASSPATH $MAIN_CLASS $@
