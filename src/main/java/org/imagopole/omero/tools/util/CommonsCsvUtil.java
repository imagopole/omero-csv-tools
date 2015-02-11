/**
 *
 */
package org.imagopole.omero.tools.util;

import org.apache.commons.csv.CSVFormat;

/**
 * Shared constants and utility methods for CSV settings.
 *
 * @author seb
 *
 */
public final class CommonsCsvUtil {

    /** Default CSV format for this application. */
    public static final CSVFormat DEFAULT_CSV_FORMAT =
        CSVFormat.DEFAULT.withIgnoreEmptyLines(true).withIgnoreSurroundingSpaces(true);

    /** Utility class. */
    private CommonsCsvUtil() {
        super();
    }

    /**
     * Default format with custom delimiter and header settings.
     *
     * @param delimiter the delimiter character
     * @param skipHeader should the header line be ignored
     * @return a new CSVFormat instance based on the default format
     */
    public static CSVFormat defaultFormatWith(Character delimiter, boolean skipHeader) {
        Check.notNull(delimiter, "delimiter");

        return DEFAULT_CSV_FORMAT.withDelimiter(delimiter).withSkipHeaderRecord(skipHeader);
    }
}
