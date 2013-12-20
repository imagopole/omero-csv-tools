/**
 *
 */
package org.imagopole.omero.tools.impl.cli;

import static org.imagopole.omero.tools.util.ParseUtil.empty;
import gnu.getopt.LongOpt;

import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author seb
 *
 */
public class CliArgsParser extends AbstractArgsParser {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CliArgsParser.class);

    private static final String USAGE_FORMAT =
           "\n"
           + "Example usage: \n"
           + "\n"
           + "  %s -s localhost -u my-username -w secret --annotated-type dataset "
           +      "--annotation-type tag --csv-container-type local --csv-container-id 1234 \n"
           +"\n"
           + "Run: %s -h or --help for extended options";

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
            + "      --annotated-type            Type of annotated objects (eg. project, dataset, "
            +                                    "image being linked to). \n"
            + "                                  Valid values: 'dataset' \n"    // TODO: 'image', 'project?'
            + "\n"
            + "      --annotation-type           Type of annotation. \n"
            + "                                  Valid values: 'tag' \n"
            + "\n"
            + "      --csv-container-type        Type of object used to source the csv file from. \n"
            + "                                  Valid values: 'project', 'local' \n"
            + "\n"
            + "      --csv-container-id          Identifier of the remote top-level container. \n"
            + "                                  Serves as a parent to filter the specified "
            +                                    "annotated-type targets. \n"
            + "                                  If 'csv-container-type' is set to a 'remote' "
            +                                    "container type such as 'project', 'dataset' or "
            +                                    "'image', also serves as the csv file attachment "
            +                                    "bearing container. \n "
            + "\n"
            + "Optional arguments:"
            + "\n"
            + "  -h, --help                      Display this help \n"
            + "\n"
            + "  -n, --dry-run                   Process csv file but do not save annotations \n"
            + "                                  Valid values: true, false \n"
            + "                                  Default value: false \n"
            + "\n"
            + "  -p, --port                      OMERO server port \n"
            + "                                  Default value: 4064 \n"
            + "\n"
            + "      --csv-file-name             Name of CSV file \n"
            + "                                  Default value: as per naming convention: "
            +                                    "{annotated-type}_{annotation-type}.csv"
            + "                                  Eg. dataset_tag.csv, image_comment.csv \n"
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
            + "Options examples:"
            + "\n"
            + "  * Apply tags to datasets within project 1234 from a local csv file: \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-container-type local --csv-file-name=/tmp/some-file.csv --csv-container-id 1234 \n"
            + "\n"
            + "  * Apply tags to datasets within project 1234 from a local csv file with the default "
            + "conventional name ('dataset_tag.csv'): \n"
            + "  -s localhost -u my-username -w secret --annotated-type dataset --annotation-type tag "
            + "--csv-container-type local --csv-container-id 1234 \n"
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
            + "Expected CSV format (with optional columns header): \n"
            + "------------------------------------------------------------------\n"
            + "name of annotated type, variable-length list of annotation values \n"
            + "dataset_1, tag_one, tag_two \n"
            + "dataset_2, \"tag_three_is_quoted\" \n"
            ;

    private static final String SHORT_OPTIONS = "s:u:w:p:h";

    private static final LongOpt[] LONG_OPTIONS = new LongOpt[] {

        //-- required args:
        //   -- core args: keep naming in sync with the CLI importer tool
        new LongOpt("hostname", LongOpt.REQUIRED_ARGUMENT, null, 's'),
        new LongOpt("username", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
        new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'w'),

        //   -- app-specific args
        new LongOpt("annotated-type",     LongOpt.REQUIRED_ARGUMENT, null, 01),
        new LongOpt("annotation-type",    LongOpt.REQUIRED_ARGUMENT, null, 02),
        new LongOpt("csv-container-type", LongOpt.REQUIRED_ARGUMENT, null, 03),
        new LongOpt("csv-container-id",   LongOpt.REQUIRED_ARGUMENT, null, 04),

        //-- optional args:
        //   -- core args: keep naming in sync with the CLI importer tool
        new LongOpt("port",    LongOpt.OPTIONAL_ARGUMENT, null, 'p'),
        new LongOpt("help",    LongOpt.OPTIONAL_ARGUMENT, null, 'h'),
        new LongOpt("dry-run", LongOpt.OPTIONAL_ARGUMENT, null, 'n'),
        //TBD: new LongOpt("debug",     LongOpt.OPTIONAL_ARGUMENT, null, '??'),

        //   -- app-specific args
        new LongOpt("csv-file-name",   LongOpt.OPTIONAL_ARGUMENT, null, 10),
        new LongOpt("csv-delimiter",   LongOpt.OPTIONAL_ARGUMENT, null, 20),
        new LongOpt("csv-skip-header", LongOpt.OPTIONAL_ARGUMENT, null, 30),
        new LongOpt("csv-charset",     LongOpt.OPTIONAL_ARGUMENT, null, 40)
    };

    /**
     *
     * @param applicationName
     */
    public CliArgsParser(String programCommand) {
        super(programCommand, SHORT_OPTIONS, LONG_OPTIONS);
    }

    @Override
    public String getUsage() {
        return String.format(USAGE_FORMAT, getProgramName(), getProgramName());
    }

    @Override
    public String getHelp() {
        return String.format(HELP_FORMAT, getProgramName());
    }

    @Override
    protected boolean validate(CsvAnnotationConfig config, StringBuffer validationMessages) {
        boolean valid = true;

        // check for basic presence/absence
        if (empty(config.getHostname()))            { valid = false; validationMessages.append("\n hostname ");          }

        if (empty(config.getUsername()))            { valid = false; validationMessages.append("\n username ");          }
        if (empty(config.getPassword()))            { valid = false; validationMessages.append("\n password");           }

        if (empty(config.getAnnotatedTypeArg()))    { valid = false; validationMessages.append("\n annotated-type");     }
        if (empty(config.getAnnotationTypeArg()))   { valid = false; validationMessages.append("\n annotation-type");    }
        if (empty(config.getCsvContainerTypeArg())) { valid = false; validationMessages.append("\n csv-container-type"); }
        if (null == config.getContainerId())        { valid = false; validationMessages.append("\n csv-container-id");   }
        if (null == config.getCsvDelimiter())       { valid = false; validationMessages.append("\n csv-delimiter");      }
        if (null == config.getCsvSkipHeader())      { valid = false; validationMessages.append("\n csv-skip-header");    }
        if (null == config.getCsvCharsetName())     { valid = false; validationMessages.append("\n csv-charset");        }
        if (null == config.getPort())               { valid = false; validationMessages.append("\n port");               }
        if (null == config.getDryRun())             { valid = false; validationMessages.append("\n dry-run");            }

        log.debug("validConfig? {}", valid);

        return valid;
    }

}
