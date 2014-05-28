#!/bin/bash

####
# OMERO CSV Annotation Tool Jar dependencies
# Includes both individual dependencies and standalone distribution
#

# Distribution unpack directory
# Assumed to be the current folder for the executed script
BIN_DIR=`dirname $0`
UNPACK_DIR=`readlink -e "$BIN_DIR/.."`
JARS_DIR="$UNPACK_DIR/lib"
CSV_TOOL_VERSION="0.2.4-SNAPSHOT"

####
# Dependencies provided by the "with-dependencies" distribution 
# (ie. with split individual classpath components)

# SLF4J binding implementation jars
# Note: all logging output may be suppressed by using the SLF4J default no-op logger
# (ie. by excluding the LOGGER_BINDINGS_CLASSPATH from the main CLASSPATH)
LOGGER_BINDINGS_CLASSPATH=\
$JARS_DIR/logback-core-1.0.9.jar:\
$JARS_DIR/logback-classic-1.0.9.jar

# OMERO Blitz client bindings and dependencies
OMERO_CLIENT_CLASSPATH=\
$JARS_DIR/hibernate-annotations-3.5.6-Final.jar:\
$JARS_DIR/hibernate-core-3.5.6-Final.jar:\
$JARS_DIR/hibernate-jpa-2.0-api-1.0.0.Final.jar:\
$JARS_DIR/hibernate-search-3.1.1.GA.jar:\
$JARS_DIR/omero_client-5.0.0-beta1-ice34.jar

# CSV Annotation Tool dependencies
CSV_ANNOTATION_TOOL_CLASSPATH=\
$JARS_DIR/jcl-over-slf4j-1.7.5.jar:\
$JARS_DIR/slf4j-api-1.7.5.jar:\
$JARS_DIR/commons-csv-1.0-r1534206.jar:\
$JARS_DIR/guava-13.0.jar:\
$JARS_DIR/java-getopt-1.0.13.jar:\
$JARS_DIR/jsr305-1.3.7.jar:\
$UNPACK_DIR/omero-csv-tools-$CSV_TOOL_VERSION.jar

# Full classpath for the "with-dependencies" distribution
DEPENDENCIES_CLASSPATH="$JARS_DIR:$LOGGER_BINDINGS_CLASSPATH:$OMERO_CLIENT_CLASSPATH:$CSV_ANNOTATION_TOOL_CLASSPATH"

####
# Full classpath for the "standalone" distribution
# May be used in place of DEPENDENCIES_CLASSPATH (dependencies merged into a fat jar)
STANDALONE_CLASSPATH="$UNPACK_DIR/omero-csv-tools-$CSV_TOOL_VERSION-standalone.jar"
