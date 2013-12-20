/**
 *
 */
package org.imagopole.omero.tools.impl.logic;

import java.util.Collection;

import com.google.common.collect.Multimap;

import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvLineReader;
import org.imagopole.omero.tools.api.logic.CsvReaderService;
import org.imagopole.omero.tools.impl.csv.CommonsCsvStringReader;
import org.imagopole.omero.tools.impl.csv.CsvAnnotationsReader;
import org.imagopole.omero.tools.util.AnnotationsUtil;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author seb
 *
 */
public class DefaultCsvReaderService implements CsvReaderService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvReaderService.class);

    /** Delimiter character for CSV file parsing */
    private Character delimiter;

    /** Ignore first line when parsing CSV file */
    private boolean skipHeader;

    /**
     * Default constructor
     */
    public DefaultCsvReaderService() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multimap<String, String> readUniqueRecords(String content) {
        Check.notEmpty(content, "content");

        Collection<CsvAnnotationLine> lines = readLines(content);

        Multimap<String, String> uniqueRecords = AnnotationsUtil.indexByRow(lines);
        log.debug("uniqueRecords: {} - {}", uniqueRecords.keys().size(), uniqueRecords);

        return uniqueRecords;
    }

    private Collection<CsvAnnotationLine> readLines(String content) {
        Check.notEmpty(content, "content");

        // read content into minimally cleansed lines
        Collection<CsvAnnotationLine> lines = buildReader(content).getLines();

        rejectIfEmpty(lines);

        log.debug("lines: {}", lines.size());

        return lines;
    }

    private void rejectIfEmpty(Collection<CsvAnnotationLine> records) {
        if (null == records || records.isEmpty()) {
            throw new IllegalStateException(String.format(
                "No csv records found - check that file is not empty " +
                "and that the delimiter matches the content (was: %s)", getDelimiter()));
        }
    }

    //FIXME: "inject" this dependency
    private CsvLineReader<CsvAnnotationLine> buildReader(String content) {
        CsvLineReader<CsvAnnotationLine> csvReader =
            new CsvAnnotationsReader(
                CommonsCsvStringReader.getReader(content, getDelimiter(), isSkipHeader()));

        return csvReader;
    }

    /**
     * @return the skipHeader
     */
    public boolean isSkipHeader() {
        return skipHeader;
    }

    /**
     * @param skipHeader the skipHeader to set
     */
    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }

    /**
     * Returns delimiter.
     * @return the delimiter
     */
    public Character getDelimiter() {
        return delimiter;
    }

    /**
     * Sets delimiter.
     * @param delimiter the delimiter to set
     */
    public void setDelimiter(Character delimiter) {
        this.delimiter = delimiter;
    }
}
