/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import static org.imagopole.omero.tools.util.AnnotationsUtil.getMaxAnnotationsSize;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvHeader;
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

    /** Internal CSV header formatter. */
    private CsvHeader csvHeader;

    /**
     * Instanciates a CSV writer for the given format and file content.
     *
     * @param format the Apache Commons CSV format
     * @param csvHeader the internal header formatter
     */
    private CommonsCsvAnnotationsWriter(CSVFormat format, CsvHeader csvHeader) {
        super();

        Check.notNull(format, "format");
        Check.notNull(csvHeader, "csvHeader");

        this.format = format;
        this.csvHeader = csvHeader;
    }

    /**
     * Builds a CSV writer for the default format.
     *
     * @return the CSV line writer
     */
    public static CsvLineWriter<CsvAnnotationLine> defaultWriter() {
        return new CommonsCsvAnnotationsWriter(
            CommonsCsvUtil.DEFAULT_CSV_FORMAT, DefaultCsvHeader.create());
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

        return new CommonsCsvAnnotationsWriter(customFormat, DefaultCsvHeader.create());
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

         handleComments(printer, getCsvHeader().getComments());
         handleHeader(printer, lines);
         handleBody(printer, lines);
    }

    private void handleComments(CSVPrinter printer, Collection<String> comments) throws IOException {
        if (null != comments && !comments.isEmpty()) {

            for (String comment : comments) {
                handleComment(printer, comment);
            }

        }
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

         // make sure there really is a first row that can be ignored, since the default script
         // behaviour is to ignore the first row when used for tagging
         if (skipHeader) {

             // get the number of columns to be generated in the header
             int maxAnnotationsCount = getMaxAnnotationsSize(lines);

             // generate the header row to fit the number of columns
             Collection<String> header = getCsvHeader().getRecord(maxAnnotationsCount);

             if (null != header && !header.isEmpty()) {
                 printer.printRecord(header);
             }

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
     * Returns headerFormat.
     * @return the headerFormat
     */
    public CsvHeader getCsvHeader() {
        return csvHeader;
    }

    /**
     * Sets headerFormat.
     * @param headerFormat the headerFormat to set
     */
    public void setCsvHeader(CsvHeader headerFormat) {
        this.csvHeader = headerFormat;
    }

}
