/**
 *
 */
package org.imagopole.omero.tools.api.cli;

/**
 * Command line argument parser for the CSV tool's parameters.
 *
 * @author seb
 *
 */
public interface ArgsParser {

    /** Parse command line arguments into a config object. */
    CsvAnnotationConfig parseArgs(String... args);

    /** Short output to indicate how to display the extended help. */
    String getUsage();

    /** Extended usage with configuration options details. */
    String getHelp();

    /** Is the current option --help? */
    boolean isHelp();
}
