# OMERO CSV Annotation Tool deployment as an OMERO.script


## Minimum requirements

- OMERO.server-5.0.0-beta1-ice34
- Java 6
- Python 2.6
- GNU bash 4.2


## Get the standalone distribution

- Download `omero-csv-tools-VERSION-dist-standalone.zip`

- Unpack to a location of your choice.

Example - assuming deployment to `/opt/omero/lib/omero-csv-annotation-tool`:

    mkdir -p /opt/omero/lib/omero-csv-annotation-tool
    unzip omero-csv-tools-VERSION-dist-standalone.zip -d /opt/omero/lib/omero-csv-annotation-tool


## Configure the server scripting environment

- Deploy the OMERO scripts located in the `annotation_scripts` distribution folder to your OMERO server:
  add the `annotation_scripts` directory to your `OMERO.server/lib/scripts/omero/` directory.

- Check that the CSV tool script `csv-annotation-tool-script.sh` is executable for the OMERO user,
  or `chmod u+x csv-annotation-tool-script.sh`

- Make sure that this script is available on the PATH environment variable - hence accessible to the
  OMERO.server scripting environment: `export PATH=$PATH:/opt/omero/lib/omero-csv-annotation-tool`.


## Run the script

- Upload a CSV file as an attachment to an OMERO project or dataset.

- Launch the script from OMERO.server, OMERO.insight or OMERO.web via
  `Scripts > Annotation Scripts > CSV Annotation Tool`.


# OMERO CSV Annotation Tool deployment as a standalone CLI tool

- As above, unpack the distribution (either standalone or with dependencies).

- Run `csv-annotation-tool-cli.sh` for usage arguments.


# Miscellaneous

## Alternate deployment options

Two binary distributions are supplied:
- _standalone_: contains all the required classpath dependencies bundled into a single jar file (ie. uberjar
  distribution).
- _with dependencies_: provides all required classpath dependencies as individual jars.

Depending on your needs and preferences, you may choose either - the standalone distribution allows deployment
from a single package, whereas using separate dependencies provides a more fine grained classpath management
(eg. cherry-picking jars or shell scripts to a different location).


## Reference documentation

- OMERO.scripts guide: http://www.openmicroscopy.org/site/support/omero5/developers/scripts/user-guide.html
