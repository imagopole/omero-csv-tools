#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

from datetime import datetime

import omero
import omero.scripts as scripts
from omero.rtypes import unwrap, rstring, rlong

##
# This script expects the external Java tool to be available as
# an executable command exposed on the PATH under a conventional name.
#
# The external command will locate the relevant
# classpath dependencies and lauch the Java tool.
##

##
# The expected name for the executable command on the PATH
##
COMMAND_NAME = "csv-annotation-tool-script.sh"

##
# Command line arguments format for the Java process:
# {0} = COMMAND_NAME ('csv-annotation-tool-script.sh' by default)
# {1} = OMERO session token
# {2} = list of additional arguments in SINGLE_ARGUMENT_FORMAT
##
COMMAND_PATH_FORMAT = "{0} --hostname='localhost' --session-key='{1}' {2} --annotation-type=tag"

##
# Format for a single argument passed to the Java command line tool.
# Follows the Getopt long format (ie. --arg-name=value)
##
SINGLE_ARGUMENT_FORMAT = "--{0}='{1}'"

##
# OMERO script enumeration values for combo-boxes
# All values follow the script style guidelines for data binding and
# are converted to their lowercase equivalent before the external
# process invocation
##
ANNOTATED_TYPES_ENUM = [
    rstring("Dataset"),
    rstring("Image")
]

# Keep UI minimal at this point - to be added later if extra annotation types are introduced
#ANNOTATION_TYPES_ENUM = [
#    rstring("Tag")
#]

CONTAINER_TYPES_ENUM = [
    rstring("Project"),
    rstring("Dataset")
]

##
# An enum-style holder for the script
# parameters and their keys/labels
##
class Labels:
    ANNOTATED_TYPE  = "Target_For_Annotation"
    ANNOTATION_TYPE = "Annotation_Type"
    DATA_TYPE       = "Data_Type"
    IDs             = "IDs"
    FILE_NAME       = "CSV_File_Name"
    DELIMITER       = "CSV_Records_Separator"
    SKIP_HEADER     = "Skip_CSV_Header_Line"
    CHARSET         = "CSV_Text_Encoding"
    #DRY_RUN         = "Simulation_Mode"

##
# One-to-one mapping for conversion between:
# - the script parameters keys(following the style guidelines
#   and taking advantage of the variables binding magic)
# - the external command arguments keys
#
# key   = OMERO script input parameter key
# value = CSV Annotation Tool CLI argument key
##
PARAMETERS_KEYS_MAPPING = {
    Labels.ANNOTATED_TYPE  : "annotated-type",
    Labels.ANNOTATION_TYPE : "annotation-type",
    Labels.DATA_TYPE       : "csv-container-type",
    Labels.IDs             : "csv-container-id",
    Labels.FILE_NAME       : "csv-file-name",
    Labels.DELIMITER       : "csv-delimiter",
    Labels.SKIP_HEADER     : "csv-skip-header",
    Labels.CHARSET         : "csv-charset"
    #Labels.DRY_RUN         : "dry-run" 
}

##
# Helper functions and one-to-one mapping for conversion between:
# - the script parameters values
# - the external command arguments values
#
# key   = OMERO script input parameter key
# value = single-arg function to be applied for conversion  
#         between the OMERO script parameter value and its
#         CSV tool counterpart.
#         If None, considered pass-through.
##
def trim(value):
    if value is not None:
        return str(value).strip()
    return value

def to_lowercase(value):
    if value is not None:
        return trim(value).lower()
    return value

def to_uppercase(value):
    if value is not None:
        return trim(value).upper()
    return value

def first_item_or_none(list_value):
    if list_value:
        return list_value[0]
    return None

PARAMETERS_VALUES_MAPPING = {
    Labels.ANNOTATED_TYPE  : to_lowercase,
    Labels.ANNOTATION_TYPE : to_lowercase,
    Labels.DATA_TYPE       : to_lowercase,
    Labels.IDs             : first_item_or_none,
    Labels.FILE_NAME       : trim,
    Labels.DELIMITER       : trim,
    Labels.SKIP_HEADER     : to_lowercase,
    Labels.CHARSET         : to_uppercase
    #Labels.DRY_RUN         : to_lowercase
}


def log(msg):
    """
    Output message to stdout. May buffer for internal logging.

    @param msg the message to log
    """

    print msg


def lookup_external_key(client_key):
    """
    Gets the CSV tool argument key equivalent to this OMERO script argument.

    @param client_key the OMERO script key
    """

    external_key = PARAMETERS_KEYS_MAPPING[client_key]
    log("lookup_external_key: {0}={1}".format(client_key, external_key))

    return external_key


def convert_to_external_value(client_key, client_value):
    """
    Converts OMERO script argument value to a format compatible with the CSV tool.

    @param client_key the OMERO script key
    @param client_value the OMERO script argument value
    """

    external_value = client_value
    if client_value is not None:
        converter_function = PARAMETERS_VALUES_MAPPING[client_key]
        if converter_function is not None:
            external_value = converter_function(client_value)
        else:
            external_value = str(client_value)

    log("convert_to_external_value: {0}={1}".format(client_value, external_value))

    return external_value


def get_client_parameters_map(client):
    """
    Build the input parameters supplied to the script as a dictionary using the
    keys provided by the script engine.

    @param client the OMERO Blitz client
    """

    client_params = {}
    for key in client.getInputKeys():
        if client.getInput(key):
            param_value = unwrap(client.getInput(key))
            client_params[key] = param_value

    log("get_client_parameters_map: {0}".format(client_params))

    return client_params


def get_external_parameters_map(client):
    """
    Build the input parameters supplied to the script as a dictionary converting the
    keys provided by the script engine to the keys supported by the external program.

    @param client the OMERO Blitz client
    """

    external_params = {}

    client_parameters = get_client_parameters_map(client)
    for key in client_parameters:
        client_value = client_parameters[key]
        external_key = lookup_external_key(key)
        external_params[external_key] = convert_to_external_value(key, client_value)

    log("get_external_parameters_map: {0}".format(external_params))

    return external_params


def format_external_parameters(parameters_map):
    """
    Converts the OMERO script arguments (keys and values) to their command line
    counterparts as expected by the CSV tool.

    @param parameters_map the key-value pairs to be passed to the external command
    """

    formatted_params = []
    for key in parameters_map:
        param_value = parameters_map[key]
        param = SINGLE_ARGUMENT_FORMAT.format(key, param_value)
        formatted_params.append(param)

    log("format_external_parameters: {0}".format(formatted_params))

    return formatted_params


def build_external_command(parameters_map, session_key):
    """
    Build CsvAnnotator command line parameters.

    @param parameters_map the key-value pairs to be passed to the external command
    @param session_key the OMERO session token
    """

    parameters_list = format_external_parameters(parameters_map)
    parameters_string = " ".join(parameters_list)

    # command exposed on PATH under the default name
    cmd = COMMAND_PATH_FORMAT.format(COMMAND_NAME, session_key, parameters_string)

    log("build_external_command: {0}".format(cmd))

    return cmd


def run_external_command(cmd):
    """
    Call CsvAnnotator via command line with parameters.

    @param cmd the external program to launch
    """

    ret_val = os.system(cmd)
    log("run_external_command: {0}".format(ret_val))

    return ret_val


def build_output_message(ret_val):
    """
    Converts the external process return value to a more user-friendly message for display.

    @param ret_val the return value from the external process execution
    """
    status_msg = "failure"
    if ret_val == 0:
        status_msg = "success"

    message = "Processing done ({0}).".format(status_msg)
    log("build_output_message {0}".format(message))

    return message


def run_as_script():
    """
    The main entry point of the script, as called by the client via the scripting service,
    passing the required parameters.
    """

    client = scripts.client(

    #---- script name and description
    "Csv_Annotation_Tool.py",

    """
    Bulk annotate your data in an unattended manner using definitions retrieved from a CSV file.

    In order to tag datasets within a project, attach the CSV file to the project.
    In order to tag images within a dataset, attach the CSV file to the dataset.

    A detailed user guide is available in the distributed "csv-annotation-tool-manual".
    """,

    #---- Required arguments
    scripts.String(
        Labels.DATA_TYPE,
        optional = False,
        grouping = "1",
        description = "The data type (eg. project or dataset) bearing the attached CSV file (and 'parent' of the data you want to annotate)",
        values = CONTAINER_TYPES_ENUM
        ),

    scripts.List(
        Labels.IDs,
        optional = False,
        grouping = "1",
        description = "Identifier of the data type above (CSV file holder and parent of the data to be annotated). Please input only one ID."
        ).ofType(rlong(0)),

    # Keep script UI minimal for now - may reintroduce this later on if additional annotation types are supported
    #scripts.String(
    #    Labels.ANNOTATION_TYPE,
    #    optional = False,
    #    grouping = "2",
    #    description = "The type of annotation to use (eg. tag, comment, etc.)",
    #    values = ANNOTATION_TYPES_ENUM
    #    ),

    scripts.String(
        Labels.ANNOTATED_TYPE,
        optional = False,
        grouping = "1",
        description = "The type of data you want to annotate (eg. dataset or image)",
        values = ANNOTATED_TYPES_ENUM
        ),

    #---- Optional arguments
    scripts.String(
        Labels.FILE_NAME,
        optional = True,
        grouping = "2",
        description = "Name of the CSV file, with extension (eg. 'my_project.csv'). Leave empty to use the default naming convention: {annotated-type}_{annotation-type}.csv"
        ),

    scripts.String(
        Labels.DELIMITER,
        optional = True,
        grouping = "2.1",
        description = "The CSV file delimiter character. Leave empty to use the default value, ie. a comma (,)."
        ),

    scripts.Bool(
        Labels.SKIP_HEADER,
        optional = True,
        grouping = "2.2",
        description = "Ignore the first (header) line from the CSV file. Default value: enabled. Tick to process all lines.",
        default = True
        ),

    # May be replace with a pre-built selection list from:
    # http://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html
    scripts.String(
        Labels.CHARSET,
        optional = True,
        grouping = "2.3",
        description = "CSV file charset encoding to use when reading. Default value: UTF-8. Leave blank if unknown."
        ),

    # Candidate for removal
    # scripts.Bool(
    #    Labels.DRY_RUN,
    #   optional = True,
    #    grouping = "5",
    #    description = "Process csv file only (stop before saving results to database). Default value: false",
    #    ),

    #---- script metadata
    version      = "0.2.1 [OMERO-5.0.0-beta1-ice34]",
    authors      = [ "S. Simard", "Imagopole" ],
    institutions = [ "Institut Pasteur" ],
    contact      = "ssimard@pasteur.fr"
    )

    session_key = None
    try:
        start_time = datetime.now()
        log("Processing started at {0}".format(start_time))

        # prepare the script parameters to arguments for the external tool
        # (including the current experimenter's session token)
        script_params = get_external_parameters_map(client)
        session_key = client.getSessionId()

        # build the foreign process launch command
        cmd = build_external_command(script_params, session_key)

        # invoke the command
        ret_val = run_external_command(cmd)

        stop_time = datetime.now()
        duration = stop_time - start_time
        log("Processing stopped at {0} with duration {1}".format(start_time, duration))

        # return the results to the client
        message = build_output_message(ret_val)
        client.setOutput("Message", rstring(message))
    finally:
        # make sure client resources are released
        if session_key is not None:
            client.closeSession()
            client.__del__()


if __name__ == "__main__":
    run_as_script()
