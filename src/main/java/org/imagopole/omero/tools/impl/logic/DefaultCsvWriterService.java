/**
 *
 */
package org.imagopole.omero.tools.impl.logic;

import java.util.Collection;
import java.util.Comparator;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvHeader;
import org.imagopole.omero.tools.api.csv.CsvLineWriter;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.api.logic.CsvWriterService;
import org.imagopole.omero.tools.impl.csv.CommonsCsvAnnotationsWriter;
import org.imagopole.omero.tools.impl.csv.DefaultCsvHeader;
import org.imagopole.omero.tools.util.AnnotationsUtil;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.PojosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

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
    public String writeLines(
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            Collection<PojoData> pojos) throws RtException {

        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");
        Check.notEmpty(pojos, "pojos");

        // convert and reorder records before writing
        Comparator<String> exportComparator = buildLineComparator();

        Collection<CsvAnnotationLine> lines =
            PojosUtil.toSortedCsvAnnotationLines(pojos, exportComparator);

        log.debug("Converted lines from pojos: {} with ordering: {}",
                  lines.size(), exportComparator.getClass());

        // generate column headers based on the actual data types being processed
        CsvHeader header = buildCsvHeaderFormatter(annotatedType, annotationType);

        return writeCsvAnnotationLines(lines, header);
    }

    private String writeCsvAnnotationLines(Collection<CsvAnnotationLine> lines, CsvHeader header) throws RtException {
        Check.notEmpty(lines, "lines");
        Check.notNull(header, "header");

        // format content into CSV records
        String result = buildCsvWriter(header).toCsv(lines);

        return result;
    }

    private CsvHeader buildCsvHeaderFormatter(AnnotatedType annotatedType, AnnotationType annotationType) {
        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(annotationType, "annotationType");

        return DefaultCsvHeader.create(
            CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, annotatedType.name()),
            CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, annotationType.name()));
    }

    private CsvLineWriter<CsvAnnotationLine> buildCsvWriter(CsvHeader header) {
        return CommonsCsvAnnotationsWriter.getWriter(getDelimiter(), isSkipHeader(), header);
    }

    private Comparator<String> buildLineComparator() {
        // TODO: we may wish to make the comparator implementation fully configurable via
        // a cli option so user can have control over default sort or alphanumeric sort?
        return AnnotationsUtil.EXPORT_LINE_COMPARATOR;
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
