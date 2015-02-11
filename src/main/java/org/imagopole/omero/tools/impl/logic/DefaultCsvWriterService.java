/**
 *
 */
package org.imagopole.omero.tools.impl.logic;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvLineWriter;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.api.logic.CsvWriterService;
import org.imagopole.omero.tools.impl.csv.CommonsCsvAnnotationsWriter;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.PojosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer to the CSV writing/printing application logic.
 *
 * @author seb
 *
 */
public class DefaultCsvWriterService implements CsvWriterService {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvWriterService.class);

    /** Delimiter character for CSV file parsing. */
    private Character delimiter;

    /** Ignore first line when parsing CSV file. */
    private boolean skipHeader;

    /**
     * Default constructor.
     */
    public DefaultCsvWriterService() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeLines(Collection<PojoData> pojos) throws RtException {
        Check.notEmpty(pojos, "pojos");

        // convert and reorder records before writing
        Collection<CsvAnnotationLine> lines = PojosUtil.toSortedCsvAnnotationLines(pojos);

        log.debug("Converted lines from pojos: {}", lines.size());

        return writeCsvAnnotationLines(lines);
    }

    private String writeCsvAnnotationLines(Collection<CsvAnnotationLine> lines) throws RtException {
        Check.notEmpty(lines, "lines");

        // format content into CSV records
        String result = buildCsvWriter().toCsv(lines);

        return result;
    }

    private CsvLineWriter<CsvAnnotationLine> buildCsvWriter() {
        return CommonsCsvAnnotationsWriter.getWriter(getDelimiter(), isSkipHeader());
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

    /**
     * Returns skipHeader.
     * @return the skipHeader
     */
    public boolean isSkipHeader() {
        return skipHeader;
    }

    /**
     * Sets skipHeader.
     * @param skipHeader the skipHeader to set
     */
    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }

}
