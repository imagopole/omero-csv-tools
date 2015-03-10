/**
 *
 */
package org.imagopole.omero.tools.impl.cli;

import static org.imagopole.omero.tools.util.ParseUtil.empty;
import gnu.getopt.LongOpt;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.Args.FileType;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line arguments parser for use as a CLI tool.
 *
 * Requires username and password for authentication.
 *
 * @author seb
 *
 */
public class CliArgsParser extends AbstractArgsParser {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CliArgsParser.class);

    /** Short usage help. */
    private static final String USAGE_FORMAT =
           "\n"
           + "Example usage: \n"
           + "\n"
           + "  %s -s localhost -u my-username -w secret --annotated-type dataset "
           +      "--annotation-type tag --csv-container-type local --csv-container-id 1234 \n"
           + "\n"
           + "Run: %s -h or --help for extended options";

    /** Long usage help. */
    private static final String HELP_FORMAT =
            "\n"
            + "Usage: %s [OPTIONS]"
            + "\n"
            + "\n"
            + "Required arguments:"
            + "\n"
            + "  -s, --hostname                  OMERO server hostname \n"
            + "  -u, --username                  OMERO experimenter username \n"
            + "  -w, --password                  OMERO experimenter password \n"
            + "\n"
            + "      --annotation-type           Type of annotation. \n"
            + "                                  Valid values: " + AnnotationType.dump() + "\n"
            + "\n"
            + "      --csv-container-type        Type of object used to source the csv file from. \n"
            + "                                  Valid values: " + ContainerType.dump() + " \n"
            + "\n"
            + "      --csv-container-id          Identifier of the remote top-level container. \n"
            + "                                  Serves as a parent to filter the specified "
            +                                    "annotated-type targets. \n"
            + "                                  If csv-file-type is set to 'remote', also serves "
            +                                    "as the csv file attachment bearing container. \n "
            + "\n"
            + "Optional arguments:"
            + "\n"
            + "      --annotated-type            Type of annotated objects (eg. dataset, plate,"
            +                                    "image being linked to). \n"
            + "                                  When empty, defaults to the child data type "
            +                                    "within the selected csv-container-type.\n"
            + "                                  Valid values: " + AnnotatedType.dump() + "\n"
            + "\n"
            + "  -h, --help                      Display this help \n"
            + "\n"
            + "  -p, --port                      OMERO server port \n"
            + "                                  Default value: 4064 \n"
            + "\n"
            + "      --csv-file-name             Name of CSV file \n"
            + "                                  Default value: as per naming convention: "
            +                                    "{annotated-type}_{annotation-type}.csv \n"
            + "                                  Eg. dataset_tag.csv, image_comment.csv \n"
            + "\n"
            + "      --csv-file-type             Type of CSV file (local filesystem or remote "
            +                                    "server attachment)  \n"
            + "                                  Valid values: " + FileType.dump() + "\n"
            + "                                  Default value: remote \n"
            + "\n"
            + "      --csv-delimiter             CSV file delimiter character. \n"
            + "                                  Default value: comma (,) \n"
            + "\n"
            + "      --csv-skip-header           Ignore first line from CSV file. \n"
            + "                                  Valid values: true, false \n"
            + "                                  Default value: true \n"
            + "\n"
            + "      --csv-charset               CSV file charset name for decoding. \n"
            + "                                  Default value: UTF-8 \n"
            + "\n"
            + "      --export-mode               Extract annotation metadata to CSV file. \n"
            + "                                  Valid values: true, false \n"
            + "                                  Default value: false \n"
            + "\n"
            + "Options examples:"
            + "\n"
            + "  * Apply tags to datasets within project 1234 from a local csv file: \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-file-type local --csv-file-name=/tmp/some-file.csv "
            + "--csv-container-type project --csv-container-id 1234 \n"
            + "\n"
            + "  * Apply tags to datasets within project 1234 from a local csv file with the default "
            + "conventional name ('dataset_tag.csv'): \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-file-type local --csv-container-type project --csv-container-id 1234 \n"
            + "\n"
            + "  * Apply tags to datasets from a remote csv file attached to a project: \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-container-type project --csv-container-id 1234 --csv-file-name=clinical_tags.csv "
            + "--csv-container-id 1234 \n"
            + "\n"
            + "  * Apply tags to datasets from a remote csv file attached to a project with the default "
            + "conventional name ('dataset_tag.csv'): \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-container-type project --csv-container-id 1234 \n"
            + "\n"
            + "  * Export tags and datasets to a remote csv file attached to a project with the default "
            + "conventional name ('dataset_tag.export.csv'): \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-container-type project --csv-container-id 1234 --export-mode=true \n"
            + "\n"
            + "  * Export tags and datasets to a local csv file with a custom name: \n "
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-file-type local --csv-file-name=/tmp/my_datasets.csv "
            + "--csv-container-type project --csv-container-id 1234 --export-mode=true \n"
            + "\n"
            + "Expected CSV format (with optional columns header): \n"
            + "------------------------------------------------------------------\n"
            + "name of annotated type, variable-length list of annotation values \n"
            + "dataset_1, tag_one, tag_two \n"
            + "dataset_2, \"tag_three_is_quoted\" \n";

    /** The GetOpts short options spec. */
    private static final String SHORT_OPTIONS = "s:u:w:p:h";

    /** The GetOpts long options. */
    private static final LongOpt[] LONG_OPTIONS = new LongOpt[] {

        //-- required args:
        //   -- core args: keep naming in sync with the CLI importer tool
        new LongOpt("hostname", LongOpt.REQUIRED_ARGUMENT, null, 's'),
        new LongOpt("username", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
        new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'w'),

        //   -- app-specific args
        new LongOpt("annotated-type",     LongOpt.OPTIONAL_ARGUMENT, null, 01), //-- now optional
        new LongOpt("annotation-type",    LongOpt.REQUIRED_ARGUMENT, null, 02),
        new LongOpt("csv-container-type", LongOpt.REQUIRED_ARGUMENT, null, 03),
        new LongOpt("csv-container-id",   LongOpt.REQUIRED_ARGUMENT, null, 04),

        //-- optional args:
        //   -- core args: keep naming in sync with the CLI importer tool
        new LongOpt("port",    LongOpt.OPTIONAL_ARGUMENT, null, 'p'),
        new LongOpt("help",    LongOpt.OPTIONAL_ARGUMENT, null, 'h'),
        //TBD: new LongOpt("debug",     LongOpt.OPTIONAL_ARGUMENT, null, '??'),

        //   -- app-specific args
        new LongOpt("csv-file-name",   LongOpt.OPTIONAL_ARGUMENT, null, 10),
        new LongOpt("csv-delimiter",   LongOpt.OPTIONAL_ARGUMENT, null, 20),
        new LongOpt("csv-skip-header", LongOpt.OPTIONAL_ARGUMENT, null, 30),
        new LongOpt("csv-charset",     LongOpt.OPTIONAL_ARGUMENT, null, 40),
        new LongOpt("export-mode",     LongOpt.OPTIONAL_ARGUMENT, null, 50),
        new LongOpt("csv-file-type",   LongOpt.OPTIONAL_ARGUMENT, null, 60)

    };

    /**
     * Parameterized constructor.
     *
     * @param programCommand the application name
     */
    public CliArgsParser(String programCommand) {
        super(programCommand, SHORT_OPTIONS, LONG_OPTIONS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsage() {
        return String.format(USAGE_FORMAT, getProgramName(), getProgramName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelp() {
        return String.format(HELP_FORMAT, getProgramName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean validate(CsvAnnotationConfig config, StringBuffer validationMessages) {
        boolean valid = true;

        // check for basic presence/absence
        if (empty(config.getHostname()))            { valid = false; validationMessages.append("\n hostname ");          }

        if (empty(config.getUsername()))            { valid = false; validationMessages.append("\n username ");          }
        if (empty(config.getPassword()))            { valid = false; validationMessages.append("\n password");           }

        if (empty(config.getAnnotationTypeArg()))   { valid = false; validationMessages.append("\n annotation-type");    }
        if (empty(config.getCsvContainerTypeArg())) { valid = false; validationMessages.append("\n csv-container-type"); }
        if (null == config.getContainerId())        { valid = false; validationMessages.append("\n csv-container-id");   }
        if (empty(config.getCsvFileTypeArg()))      { valid = false; validationMessages.append("\n csv-file-type");      }
        if (null == config.getCsvDelimiter())       { valid = false; validationMessages.append("\n csv-delimiter");      }
        if (null == config.getCsvSkipHeader())      { valid = false; validationMessages.append("\n csv-skip-header");    }
        if (null == config.getCsvCharsetName())     { valid = false; validationMessages.append("\n csv-charset");        }
        if (null == config.getPort())               { valid = false; validationMessages.append("\n port");               }
        if (null == config.getExportMode())         { valid = false; validationMessages.append("\n export-mode");        }

        log.debug("validConfig? {}", valid);

        return valid;
    }

}
