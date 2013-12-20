/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.csv.CsvLine;
import org.imagopole.omero.tools.api.csv.CsvLineReader;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a CSV string using the Apache Commons CSV parser into a collection of <code>CsvLine</code>.
 *
 * Does not perform any cleansing or data validation: all lines are returned as is, and as defined
 * by the <code>CSVFormat</code> parameters being used.
 *
 * Note: checked exceptions (typically IO) from the underlying  Commons CSV reader are wrapped
 * and converted to runtime exceptions.
 *
 * @author seb
 *
 */
public class CommonsCsvStringReader implements CsvLineReader<CsvLine> {

    /** Default CSV format for this reader */
    private static final CSVFormat DEFAULT_CSV_FORMAT =
        CSVFormat.DEFAULT.withIgnoreEmptyLines(true).withIgnoreSurroundingSpaces(true);

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CommonsCsvStringReader.class);

    /** Apache Commons CSV format */
    private CSVFormat format;

    /** CSV content as String */
    private String fileContent;

    /**
     * Instanciates a CSV reader for the given format and file content.
     *
     * @param format the Apache Commons CSV format
     * @param fileContent the file content
     */
    private CommonsCsvStringReader(CSVFormat format, String fileContent) {
        super();

        Check.notNull(format, "format");
        this.format = format;
        this.fileContent = fileContent;
    }

    /**
     * Builds a CSV reader for the default format and file content.
     *
     * @param fileContent the file content
     * @return the CSV line reader
     */
    public static CsvLineReader<CsvLine> defaultReader(String fileContent) {
        Check.notEmpty(fileContent, "fileContent");

        return new CommonsCsvStringReader(DEFAULT_CSV_FORMAT, fileContent);
    }

    /**
     *  Builds a CSV reader for the custom format settings and file content.
     *
     * @param fileContent the file content
     * @param delimiter the CSV delimiter character
     * @param skipHeader set to true to ignore the first CSV line
     * @return the CSV line reader
     */
    public static CsvLineReader<CsvLine> getReader(
            String fileContent,
            Character delimiter,
            boolean skipHeader) {

        Check.notEmpty(fileContent, "fileContent");
        Check.notNull(delimiter, "delimiter");

        CSVFormat customFormat =
            DEFAULT_CSV_FORMAT.withDelimiter(delimiter).withSkipHeaderRecord(skipHeader);

        return new CommonsCsvStringReader(customFormat, fileContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CsvLine> getLines() throws RtException {
        Check.notNull(format, "format");
        Check.notEmpty(fileContent, "fileContent");

         // withSkipHeaderRecord(true) only works with a header definition,
         // but our columns are dynamic (no fixed number)
         // => hence we need to exclude the header anyway, without relying
         // on the commons CSV parser behaviour
        boolean skipFirstLine = getFormat().getSkipHeaderRecord();
        log.debug("Parsing CSV lines with format {} ", getFormat());

        CSVParser parser = null;
        List<CSVRecord> records = null;
        try {
            parser = CSVParser.parse(getFileContent(), getFormat());

            records = parser.getRecords();
        } catch (IOException ioe) {
            log.error("Unable to parse CSV content ", ioe);
            throw new RtException(ioe.getMessage(), ioe);
        } finally {
            if (null != parser) {
                try {
                    parser.close();
                } catch (IOException ignore) { }
            }
        }

        return toCsvLines(records, skipFirstLine);
    }

    private List<CsvLine> toCsvLines(List<CSVRecord> records, boolean skipFirstLine) {
        List<CsvLine> result = new ArrayList<CsvLine>();

        if (null != records) {

            Iterator<CSVRecord> i = records.iterator();
            if (skipFirstLine) {
                CSVRecord skipped = i.next();
                log.debug("Skipping first csv line: {}", skipped);
            }

            while (i.hasNext()) {
                CSVRecord record = i.next();
                result.add(new CommonsCsvLine(record));
            }

        }

        log.debug("Converted {} CSVRecords to {} CsvLines", records.size(), result.size());

        return result;
    }

    /**
     * A simple wrapper around an Apache Commons CSV implementation.
     *
     * @author seb
     *
     */
    private class CommonsCsvLine implements CsvLine {

        /** The wrapped Commons CSV line */
        private CSVRecord record;

        /**
         * Default constructor.
         *
         * @param record the underlying commons-csv record
         */
        private CommonsCsvLine(CSVRecord record) {
            super();

            Check.notNull(record, "record");
            this.record = record;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long getNumber() {
            return this.record.getRecordNumber();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getValueAt(int col) {
            return this.record.get(col);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getSize() {
            return this.record.size();
        }

    }

    /**
     * Returns format.
     * @return the format
     */
    public CSVFormat getFormat() {
        return format;
    }

    /**
     * Sets format.
     * @param format the format to set
     */
    public void setFormat(CSVFormat format) {
        this.format = format;
    }

    /**
     * Returns fileContent.
     * @return the fileContent
     */
    public String getFileContent() {
        return fileContent;
    }

    /**
     * Sets fileContent.
     * @param fileContent the fileContent to set
     */
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

}
