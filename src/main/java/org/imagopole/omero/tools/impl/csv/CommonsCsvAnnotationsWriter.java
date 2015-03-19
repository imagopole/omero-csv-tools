/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import static org.imagopole.omero.tools.util.AnnotationsUtil.getMaxAnnotationsSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvLineWriter;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.CommonsCsvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Writes a CSV string using the Apache Commons CSV printer from a collection of <code>CsvAnnotationLine</code>.
 *
 * Does not perform any validation or data shuffling: all lines are printed as is, and as defined
 * by the <code>CSVFormat</code> parameters being used.
 *
 * Note: checked exceptions (typically IO) from the underlying  Commons CSV printer are wrapped
 * and converted to runtime exceptions.
 *
 * @author seb
 *
 */
public class CommonsCsvAnnotationsWriter implements CsvLineWriter<CsvAnnotationLine> {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(CommonsCsvAnnotationsWriter.class);

    /** Apache Commons CSV format. */
    private CSVFormat format;

    /**
     * Instanciates a CSV writer for the given format and file content.
     *
     * @param format the Apache Commons CSV format
     */
    private CommonsCsvAnnotationsWriter(CSVFormat format) {
        super();

        Check.notNull(format, "format");
        this.format = format;
    }

    /**
     * Builds a CSV writer for the default format.
     *
     * @return the CSV line writer
     */
    public static CsvLineWriter<CsvAnnotationLine> defaultWriter() {
        return new CommonsCsvAnnotationsWriter(CommonsCsvUtil.DEFAULT_CSV_FORMAT);
    }

    /**
     *  Builds a CSV reader for the custom format settings and file content.
     *
     * @param delimiter the CSV delimiter character
     * @param skipHeader set to true to ignore the first CSV line
     * @return the CSV line writer
     */
    public static CsvLineWriter<CsvAnnotationLine> getWriter(
            Character delimiter,
            boolean skipHeader) {

        Check.notNull(delimiter, "delimiter");

        CSVFormat customFormat = CommonsCsvUtil.defaultFormatWith(delimiter, skipHeader);

        return new CommonsCsvAnnotationsWriter(customFormat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toCsv(Collection<CsvAnnotationLine> lines) throws RtException {
        Check.notEmpty(lines, "lines");

        log.debug("Writing CSV lines with format {} ", getFormat());

        StringBuffer result = new StringBuffer();
        CSVPrinter printer = null;
        try {
            printer = new CSVPrinter(result, getFormat());

            printCsvContent(printer, lines);

            printer.flush();
        } catch (IOException ioe) {
            log.error("Unable to print CSV content ", ioe);
            throw new RtException(ioe.getMessage(), ioe);
        } finally {
            if (null != printer) {
                try {
                    printer.close();
                } catch (IOException ignore) { }
            }
        }

        return result.toString();
    }

    private void printCsvContent(
            CSVPrinter printer,
            Collection<CsvAnnotationLine> lines) throws IOException {

         Check.notNull(printer, "printer");
         Check.notEmpty(lines, "lines");

         handleComment(printer, String.format(Comments.GENERATED_ON_FORMAT, new Date()));
         handleHeader(printer, lines);
         handleBody(printer, lines);
    }

    private void handleComment(CSVPrinter printer, String comment) throws IOException {
        // note: comments have to be defined on the CSVFormat instance for printing to be effective
        if (null != comment) {
            printer.printComment(comment);
        }
    }

    private void handleHeader(
            CSVPrinter printer,
            Collection<CsvAnnotationLine> lines) throws IOException {

         boolean skipHeader = getFormat().getSkipHeaderRecord();

         // issue a comment to describe the CSV schema
         handleComment(printer, Comments.HEADER_DESCRIPTION);

         // make sure there really is a first row that can be ignored, since the default script
         // behaviour is to ignore the first row when used for tagging
         if (skipHeader) {
             List<String> header = buildHeaderRecord(lines);

             printer.printRecord(header);
         }
    }

    private void handleBody(
            CSVPrinter printer,
            Collection<CsvAnnotationLine> lines) throws IOException {

         for (CsvAnnotationLine line : lines) {
             Iterable<String> csvLine = toCsvRecord(line);

             printer.printRecord(csvLine);
          }
    }

    private Iterable<String> toCsvRecord(CsvAnnotationLine line) {
        Check.notNull(line, "line");
        Check.notEmpty(line.getAnnotatedName(), "annotatedName");

        String annotatedName = line.getAnnotatedName();
        Collection<String> annotationsValues = line.getAnnotationsValues();

        List<String> lineHead = Lists.newArrayList(annotatedName);

        Iterable<String> result = null;

        if (null != annotationsValues && !annotationsValues.isEmpty()) {
            result = Iterables.concat(lineHead, line.getAnnotationsValues());
        } else {
            result = lineHead;
        }

        return result;
    }

    private List<String> buildHeaderRecord(Collection<CsvAnnotationLine> lines) {
        Check.notNull(lines, "lines");

        // get the number of columns to be generated in the header
        int maxAnnotationsCount = getMaxAnnotationsSize(lines);

        List<String> result = new ArrayList<String>(maxAnnotationsCount + 1);

        // leading column (annotation target)
        result.add(Comments.TARGET_ENTITY_NAME);

        // one column per annotation value
        for (int i = 0; i < maxAnnotationsCount; ++i) {
            // add value column number with humanized one-based index
            result.add(String.format(Comments.ANNOTATION_COLUMN_FORMAT, i + 1));
        }

        return result;
    }

    private static final class Comments {

         /** Description for the CSV layout. */
        private static final String HEADER_DESCRIPTION =
            "Schema description: <object_name><separator><list_of_annotations>";

        /** First column header (annotation target) */
        private static final String TARGET_ENTITY_NAME = "Entity name";

        /** {@link java.util.Formatter} template for remaining header columns: <code>Annotation $annotation_number</code>.*/
        private static final String ANNOTATION_COLUMN_FORMAT = "Annotation %s";

        /** {@link java.util.Formatter} template for header comments: <code>Generated on $date</code>.*/
        private static final String GENERATED_ON_FORMAT = "Generated on %s";

        /** Constants class. */
        private Comments() {
             super();
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

}
