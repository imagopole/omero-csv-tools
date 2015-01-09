#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import glob

from datetime import datetime

###
# Quick development mode/testing script
#

DETECT_CLASSPATH = True
STANDALONE_DISTRIB = True

DEPENDENCIES_JARS_LIST = [
             "logback-core-1.0.9.jar", 
             "logback-classic-1.0.9.jar", 
             "hibernate-annotations-3.5.6-Final.jar", 
             "hibernate-core-3.5.6-Final.jar", 
             "hibernate-jpa-2.0-api-1.0.0.Final.jar", 
             "hibernate-search-3.1.1.GA.jar", 
             "omero_client-5.0.0-beta1-ice34.jar",
             "jcl-over-slf4j-1.7.5.jar", 
             "slf4j-api-1.7.5.jar", 
             "commons-csv-1.1.jar",
             "guava-13.0.jar", 
             "java-getopt-1.0.13.jar", 
             "jsr305-1.3.7.jar", 
             "omero-csv-tools-0.1-SNAPSHOT.jar" 
            ]

STANDALONE_JARS_LIST = [ "omero-csv-tools-0.1-SNAPSHOT-standalone.jar" ]

# Run as OMERO.script or CLI program
#MAIN_CLASS = "org.imagopole.omero.tools.CsvAnnotatorCliMain"
MAIN_CLASS = "org.imagopole.omero.tools.CsvAnnotatorScriptMain"

JVM_OPTS = "-Xms64M -Xmx256M -Djava.awt.headless=true"

# Command line arguments format for the Java process:
# {0} = JVM_OPTS
# {1} = CLASSPATH
# {2} = MAIN_CLASS
# {3} = list of additional arguments in long format (ie. --arg-name=value)
COMMAND_FORMAT = "java {0} -cp {1} {2} {3}"

def log(msg):
    """
    Output message to stdout. May buffer for internal logging.
    """

    print msg


def build_classpath_from_list(jars_dir, jar_list):
    """
    Build CsvAnnotator runtime classpath.
    """

    cp = ""
    for jar in jar_list:
        cp += jars_dir + jar + ":"

    cp += jars_dir
      
    return cp


def build_classpath_from_paths(jars_paths):
    """
    Build CsvAnnotator runtime classpath.
    """

    cp = ":".join(jars_paths)
      
    return cp

def lookup_configured_jars_list():
    """
    """

    if STANDALONE_DISTRIB:
        return STANDALONE_JARS_LIST
    else:
        return DEPENDENCIES_JARS_LIST

def lookup_jars_list(external_dir):
    """
    
    """

    jars_list = glob.glob(external_dir + '*.jar')

    log("lookup_jars_list from {0} : {1}".format(external_dir, jars_list))

    return jars_list


def lookup_external_dir_from_env_or_current_dir():
    """
    """

    """
     Environment variables are looked up in the following order
     to determine the jar files locations required for the external tool:
       1 - OMERO_CAT_HOME
       2 - OMERO_PREFIX/lib/omero-cat
       3 - OMERO_HOME/lib/omero-cat
     If none of them are set, the current working directory is used as fallback
    """

    external_dir = os.environ.get("OMERO_CAT_HOME")
    log("lookup_external_dir_from_env/omero_cat_home: {0}".format(external_dir))

    if external_dir is None:
        omero_prefix = os.environ.get("OMERO_PREFIX")
        if omero_prefix is not None:
            external_dir = omero_prefix + "lib/omero-cat"
        else:
            omero_home = os.environ.get("OMERO_HOME")
            if omero_home is not None:
                external_dir = omero_home + "lib/omero-cat"
            else:
                external_dir = os.getcwd()         

    if not os.path.isdir(external_dir):
        raise EnvironmentError("Directory {0} does not exist".format(external_dir))

    log("lookup_external_dir_from_env_or_current_dir: {0}".format(external_dir))

    if not external_dir.endswith("/"):
        external_dir += "/"
 
    return external_dir


def build_external_command(args_list): 
    """
    Build CsvAnnotator command line parameters.
    """

    external_dir = lookup_external_dir_from_env_or_current_dir()

    if DETECT_CLASSPATH:
        jars_paths = lookup_jars_list(external_dir)
        classpath = build_classpath_from_paths(jars_paths)
    else:
        jars_list = lookup_configured_jars_list()
        classpath = build_classpath_from_list(external_dir, jars_list)

    params = " ".join(args_list)
    
    cmd = COMMAND_FORMAT.format(JVM_OPTS, classpath, MAIN_CLASS, params)

    log("build_external_command: {0}".format(cmd))
    
    return cmd


def run_external_command(cmd):
    """
    Call CsvAnnotator via command line with parameters.
    """

    ret_val = os.system(cmd)
    log("run_external_command: {0}".format(ret_val))

    return ret_val


def build_output_message(ret_val):
    """
    
    """
    status_msg = "failure"
    if ret_val == 0:
        status_msg = "success"

    message = "Processed command with {0} return code ({1})".format(status_msg, ret_val)

    return message


def run_as_script():
    """
    The main entry point of the script.
    """

    duration = None
    try:
        start_time = datetime.now()
        log("Processing started at {0}".format(start_time))

        # build the foreign process launch command
        cmd = build_external_command(sys.argv)

        # invoke the command
        ret_val = run_external_command(cmd)
        log("External command return code: {0}".format(ret_val))

        stop_time = datetime.now()
        duration = stop_time - start_time 
        log("Processing stopped at {0} with duration {1}".format(start_time, duration))

        # display the results
        message = build_output_message(ret_val)
        log(message)
    finally:
        log("Done in: {0}".format(duration))


if __name__ == "__main__":
    run_as_script()
