/**
 *
 */
package org.imagopole.omero.tools.impl.cli;

import static org.imagopole.omero.tools.util.ParseUtil.parseBooleanOrNull;
import static org.imagopole.omero.tools.util.ParseUtil.parseCharacterOrNull;
import static org.imagopole.omero.tools.util.ParseUtil.parseIntegerOrNull;
import static org.imagopole.omero.tools.util.ParseUtil.parseLongOrNull;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.imagopole.omero.tools.api.cli.ArgsParser;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Base class for command line parsers implementations.
 *
 * @author seb
 *
 */
public abstract class AbstractArgsParser implements ArgsParser {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(AbstractArgsParser.class);

    /** The command name. */
    private String programName;

    /** The GetOpts short options spec. */
    private String shortOptions;

    /** The GetOpts long options. */
    private LongOpt[] longOptions;

    /** Is help requested? */
    private boolean help = false;

    /**
     * Parameterized constructor.
     *
     * @param programName the application name
     * @param shortOptions the GetOpts short options spec
     * @param longOptions the GetOpts long options
     */
    public AbstractArgsParser(String programName, String shortOptions, LongOpt[] longOptions) {
        super();

        Check.notEmpty(programName, programName);
        Check.notEmpty(shortOptions, shortOptions);
        Preconditions.checkArgument(null != longOptions && longOptions.length > 0, "longOptions");

        this.programName = programName;
        this.shortOptions = shortOptions;
        this.longOptions = longOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CsvAnnotationConfig parseArgs(String... args) {

        final Getopt g = new Getopt(getProgramName(), args, getShortOptions(), getLongOptions());

        final CsvAnnotationConfig config = CsvAnnotationConfig.defaultConfig();

        int c = -2;
        boolean failed = false;
        boolean isHelp = false;
        while ( (c = g.getopt()) != -1 ) {

            switch (c) {

                // --annotated-type : required
                case 01:
                    String annotatedType = g.getOptarg();
                    config.setAnnotatedTypeArg(annotatedType);
                    break;

                // --annotation-type : required
                case 02:
                    String annotationType = g.getOptarg();
                    config.setAnnotationTypeArg(annotationType);
                    break;

                // --csv-container-type : required
                case 03:
                    String containerType = g.getOptarg();
                    config.setCsvContainerTypeArg(containerType);
                    break;

                // --csv-container-id : required
                case 04:
                    String containerId = g.getOptarg();
                    config.setContainerId(parseLongOrNull(containerId));
                    break;

                // --csv-file-name : optional
                case 10:
                    String csvFileName = g.getOptarg();
                    config.setCsvFileName(csvFileName);
                    break;

                // --csv-delimiter : optional
                case 20:
                    String csvDelimiter = g.getOptarg();
                    config.setCsvDelimiter(parseCharacterOrNull(csvDelimiter));
                    break;

                 // --csv-skip-header : optional
                case 30:
                    String csvSkipHeader = g.getOptarg();
                    config.setCsvSkipHeader(parseBooleanOrNull(csvSkipHeader));
                    break;

                // --csv-charset : optional
                case 40:
                    String csvCharsetName = g.getOptarg();
                    config.setCsvCharsetName(csvCharsetName);
                    break;

                // --run-mode : optional
                case 50:
                    String runMode = g.getOptarg();
                    config.setRunModeArg(runMode);
                    break;

               // --csv-file-type : optional
                case 60:
                    String csvFileType = g.getOptarg();
                    config.setCsvFileTypeArg(csvFileType);
                    break;

                // --hostname : required
                case 's':
                    String hostname = g.getOptarg();
                    config.setHostname(hostname);
                    break;

                // --username : conditionally required (cli: yes, script: no)
                case 'u':
                    String username = g.getOptarg();
                    config.setUsername(username);
                    break;

                // --password : conditionally required (cli: yes, script: no)
                case 'w':
                    String password = g.getOptarg();
                    config.setPassword(password);
                    break;

                // --session-key : conditionally required (cli: no, script: yes)
                case 'k':
                    String sessionKey = g.getOptarg();
                    config.setSessionKey(sessionKey);
                    break;

                // --port : optional
                case 'p':
                    String port = g.getOptarg();
                    config.setPort(parseIntegerOrNull(port));
                    break;

                // --help
                case 'h':
                    isHelp = true; // trigger help()
                    break;

                // -- getopt parse error ('?')
                case '?':
                    failed = true; // trigger usage()
                    break;

                default :
                    failed = true; // trigger usage()
                    break;

            }

        }

        // parse failed - trigger usage()
        if (c != -1) {
            log.debug("Cli args parsing aborted");
            return null;
        }

        if (failed) {
            log.debug("Cli args parsing failed");
            return null;
        }

        if (isHelp) {
            log.debug("Help requested");
            this.help = true;
            return null;
        }

        StringBuffer validationMessages = new StringBuffer();
        boolean isConfigValid = validate(config, validationMessages);

        if (!isConfigValid) {
            log.error("Invalid configuration for parameters: {}", validationMessages.toString());
            return null;  // trigger usage()
        }

        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHelp() {
       return this.help;
    }

    /**
     * Check whether the config contains all required values.
     *
     * @param config the configuration to be validated
     * @param validationMessages the validation errors buffer
     * @return true if the validation passed, false otherwise
     */
    protected abstract boolean validate(CsvAnnotationConfig config, StringBuffer validationMessages);

    /**
     * @return the programCommand
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * @param programCommand the programCommand to set
     */
    public void setProgramName(String programCommand) {
        this.programName = programCommand;
    }

    /**
     * @return the longOptions
     */
    public LongOpt[] getLongOptions() {
        return longOptions;
    }

    /**
     * @param longOptions the longOptions to set
     */
    public void setLongOptions(LongOpt[] longOptions) {
        this.longOptions = longOptions;
    }

    /**
     * @return the shortOptions
     */
    public String getShortOptions() {
        return shortOptions;
    }

    /**
     * @param shortOptions the shortOptions to set
     */
    public void setShortOptions(String shortOptions) {
        this.shortOptions = shortOptions;
    }

}
