# OMERO.csvtools installation

## Minimum requirements

- OMERO.server 5.0
- Java 6
- Python 2.6
- GNU bash


## Package overview

The toolset contains three subcomponents:

- `omero-csv-tools-<VERSION>.jar`: the Java tool - may be invoked as a standalone utility or
   via its companion scripts
- `Csv_Annotation_Tool.py`: the OMERO.scripts tool - may be invoked from script-aware OMERO clients,
   and relies on a companion script/command to launch the Java tool
- `csv-annotation-tool-script.sh`: the companion shell script - acts as an executable wrapper/glu
   around the Java tool

In order to ensure compatibility between the OMERO.server and the client Blitz libraries bundled with
the toolset, it is recommended to select OMERO_FLAVORs with matching major OMERO versions for
download (eg. 'ome50x' for the '5.0.x' server line).


# OMERO.csvtools deployment as an OMERO.script

## Get the standalone distribution

- Download `omero-csv-tools-<VERSION>-<OMERO_FLAVOR>-standalone.zip`

- Unpack to a location of your choice.

Example - assuming deployment to `/opt/OMERO.csvtools`:

    mkdir -p /opt/OMERO.csvtools
    unzip omero-csv-tools-<VERSION>-dist-standalone.zip -d /opt/OMERO.csvtools


## Configure the server scripting environment

- Deploy the OMERO scripts located in the `scripts/omero/annotation_scripts` distribution folder to your OMERO server:
  add the `annotation_scripts` directory to your `OMERO.server/lib/scripts/omero/` directory.
  Alternatively, you may wish to upload the script with the built-in OMERO command line utilities:

     cd /opt/OMERO.csvtools/scripts
     $OMERO_PREFIX/bin/omero script upload -u root --official omero/annotation_scripts/Csv_Annotation_Tool.py

- Check that the companion shell script `bin/csv-annotation-tool-script.sh` is executable for the OMERO user,
  or `chmod u+x bin/csv-annotation-tool-script.sh`

- Make sure the companion shell script is available on the `PATH` environment variable - hence accessible to the
  OMERO.server scripting environment: `export PATH=$PATH:/opt/OMERO.csvtools/omero-csv-annotation-tool/bin`.
  Alternatively, you may wish to keep the `PATH` unchanged and edit `Csv_Annotation_Tool.py` prior to upload
  such that `csv-annotation-tool-script.sh` is referenced via its absolute path:
  `COMMAND_NAME = "/opt/OMERO.csvtools/bin/csv-annotation-tool-script.sh"`


## Run the script

- Upload a CSV file as an attachment to an OMERO project or dataset.

- Launch the script from OMERO.server, OMERO.insight or OMERO.web via
  `Scripts > Annotation Scripts > CSV Annotation Tool`.


# OMERO.csvtools deployment as a standalone CLI tool

- As above, unpack the distribution (either standalone or with dependencies).

- Run `cd bin && ./csv-annotation-tool-cli.sh` for usage arguments.


# Miscellaneous

## Alternate deployment options

Two binary distributions are supplied:

- _standalone_: contains all the required classpath dependencies bundled into a single jar file (ie. uberjar
  distribution).

- _with dependencies_: provides all required classpath dependencies as individual jars.

Depending on your needs and preferences, you may choose either: the standalone distribution allows deployment
from a single package, whereas using separate dependencies provides a more fine grained classpath management
(eg. cherry-picking jars and shell scripts to a different location such as `OMERO.server/lib` and
`OMERO.server/bin`, or overriding the Blitz libraries versions bundled with the tool).


## Operating system support

The "full toolset" (ie. Java tool + companion scripts) is currently unsupported on Windows environments.

Some subcomponents would require adjusting to enable Windows support:

- the Java tool should work out of the box as a CLI-only OMERO client
- the OMERO.scripts tool should work provided the COMMAND_NAME is edited accordingly
- the companion shell script would require porting to a native command interpreter, or inlining into
  the python OMERO script


## Reference documentation

- OMERO.scripts guide: http://www.openmicroscopy.org/site/support/omero5/developers/scripts/user-guide.html

- OMERO.scripts style guide: http://www.openmicroscopy.org/site/support/omero5/developers/scripts/style-guide.html

