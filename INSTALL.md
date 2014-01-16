# OMERO CSV Annotation Tool deployment as an OMERO.script


## Minimum requirements

- OMERO.server-5.0.0-beta1-ice34
- Java 6
- Python 2.6
- GNU bash 4.2


## Get the standalone distribution

- Download `omero-csv-tools-VERSION-dist-standalone.zip`

- Unpack to a location of your choice - eg. `/opt/omero/lib/omero-csv-annotation-tool`


## Configure the server scripting environment

- Deploy the OMERO scripts located in the `annotation_scripts` distribution folder to your OMERO server.
  For details see: http://www.openmicroscopy.org/site/support/omero5/developers/scripts/user-guide.html

- Check that the CSV tool script `csv-annotation-tool-script.sh` is executable for the OMERO user.

- Make sure that this script is available on the PATH environment variable (hence accessible to the
  OMERO.server scripting environment): `export PATH=$PATH:/opt/omero/lib/omero-csv-annotation-tool`.


## Run the script

- Upload a CSV file as an attachment to an OMERO project.

- Launch the script from OMERO.server, OMERO.insight or OMERO.web via
  `Scripts > Annotation Scripts > CSV Anntotation Tool`.
  See: http://www.openmicroscopy.org/site/support/omero5/developers/scripts/user-guide.html


# OMERO CSV Annotation Tool deployment as a standalone CLI tool

- As above, unpack the distribution (either standalone or with dependencies)

- Run `csv-annotation-tool-cli.sh` for usage arguments
