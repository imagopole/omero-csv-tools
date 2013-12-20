/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import java.util.Collection;

import com.google.common.collect.HashMultiset;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvLine;
import org.imagopole.omero.tools.api.csv.CsvLineReader;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and transforms CSV content into lines following the internal model.
 *
 * Follows the conventional CSV expected structure. Each line is expected to be formatted as:
 *  - the first column is the name of the annotated type (Project, Dataset, Image)
 *  - the remaining columns are the annotation values (no fixed/maximum size, with a minimum size
 *    of one annotation column)
 *
 * Performs some basic data cleansing, validation and filtering:
 * - leading/trailing spaces on columns values are trimmed
 * - empty CSV content/file is ignored
 * - empty lines are skipped
 *
 * @author seb
 *
 */
public class CsvAnnotationsReader implements CsvLineReader<CsvAnnotationLine> {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CsvAnnotationsReader.class);

    /** Delegate reader */
    private CsvLineReader<CsvLine> csvStringReader;

    /**
     * Wrapping constructor. Parsing is delegated to the underlying reader.
     *
     * @param csvStringReader the wrapped CSV content reader
     */
    public CsvAnnotationsReader(CsvLineReader<CsvLine> csvStringReader) {
        super();

        Check.notNull(csvStringReader, "csvStringReader");
        this.csvStringReader = csvStringReader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CsvAnnotationLine> getLines() throws RtException {
        Collection<CsvAnnotationLine> result = null;

        Collection<CsvLine> lines = getCsvStringReader().getLines();

        log.debug(
            "Processing CSV lines with settings " +
            "[rejectEmptyFile={}, rejectEmptyLines={}, skipEmptyLines={}, rejectMalformedLines={}]",
            ReadOptions.rejectEmptyFile, ReadOptions.rejectEmptyLines,
            ReadOptions.skipEmptyLines, ReadOptions.rejectMalformedLines);

        boolean isFileEmpty = (null == lines || lines.isEmpty());
        if (isFileEmpty) {
            result = handleEmptyFile(lines);
        } else {
            result = handleFile(lines);
        }

        return result;
    }

    private Collection<CsvAnnotationLine> handleEmptyFile(Collection<CsvLine> lines) {
        if (ReadOptions.rejectEmptyFile) {
            throw new IllegalArgumentException(String.format("CSV file is empty"));
        } else {
            return handleFile(lines);
        }
    }

    private Collection<CsvAnnotationLine> handleFile(Collection<CsvLine> lines) {
        return toCsvAnnotationLines(lines);
    }

    private Collection<CsvAnnotationLine> toCsvAnnotationLines(Collection<CsvLine> lines) {
        Collection<CsvAnnotationLine> result = HashMultiset.create();

        if (null != lines) {

            for (CsvLine line : lines) {
                // perform basic cleanup (trimming) and validation of line structure
                // duplicates are _not_ removed
                CsvAnnotationLine interpretedLine = toCsvAnnotationLine(line);

                // need to check for null lines here as empty/malformed lines will
                // be transformed to null. Maybe use NullSafe enum instead?
                if (null != interpretedLine) {
                    result.add(interpretedLine);
                }
            }

        }

        return result;
    }

    private CsvAnnotationLine toCsvAnnotationLine(CsvLine line) {
        CsvAnnotationLine result = null;

        if (null != line) {

            // to be considered valid, an annotation line must have:
            // - exactly one container name (eg dataset name)
            // - at least one annotation
            int lineSize = line.getSize();
            boolean isLineEmpty = (lineSize == 0);
            boolean isLineIncomplete = (lineSize < FileStructure.MIN_RECORDS_COUNT);

            if (isLineEmpty) {
                result = handleEmptyLine(line);
            } else if (isLineIncomplete){
                result = handleMalformedLine(line);
            } else {
                result = handleLine(line);
            }

        }

        return result;
    }

    private CsvAnnotationLine handleEmptyLine(CsvLine line) {
        Check.notNull(line, "line");

        log.debug("Empty CsvLine: #{} - {} columns", line.getNumber(), line.getSize());

        if (ReadOptions.rejectEmptyLines) {
            throw new IllegalArgumentException(
                String.format("CSV line %d empty %s", line.getNumber(), line.toString()));
        } else if (ReadOptions.skipEmptyLines) {
            return null;
        } else {
            return handleLine(line);
        }
    }

    private CsvAnnotationLine handleMalformedLine(CsvLine line) {
        Check.notNull(line, "line");

        log.trace("Malformed CsvLine: #{} - {} columns", line.getNumber(), line.getSize());

        if (ReadOptions.rejectMalformedLines) {
            throw new IllegalArgumentException(
                String.format("CSV line %d malformed or incomplete " +
                              "(at least one object name + 1 annotation required): %s",
                              line.getNumber(), line.toString()));
        } else {
            // skip invalid line. Maybe accumulate for audit?
            return null;
        }
    }

    private CsvAnnotationLine handleLine(CsvLine line) {
        Check.notNull(line, "line");

        // parse first line item: target container name
        String targetObjectName = line.getValueAt(FileStructure.CONTAINER_NAME_RECORD_INDEX);

        log.trace("Processing CsvLine: #{} {} - {} columns",
                  line.getNumber(), targetObjectName, line.getSize());

        boolean isTargetNameEmpty =
            (null == targetObjectName || targetObjectName.trim().isEmpty());

        if (isTargetNameEmpty) {
            return handleMalformedLine(line);
        }

        // parse the remainder of the line: list of annotations values
        int lineSize = line.getSize();
        HashMultiset<String> annotationsValues = HashMultiset.create(lineSize - 1);

        for (int i = FileStructure.FIRST_ANNOTATION_RECORD_INDEX; i < lineSize; ++i) {
            String columnValue = line.getValueAt(i);

            if (null != columnValue) {
                String trimmedValue = columnValue.trim();

                if (!trimmedValue.isEmpty()) {
                    annotationsValues.add(trimmedValue);
                }
            }
        }

        boolean areAnnotationValuesEmpty = annotationsValues.isEmpty();
        if (areAnnotationValuesEmpty) {
            return handleEmptyLine(line);
        }

        // build and return the object + annotations line
        CsvAnnotationLine result =
            SimpleAnnotationLine.create(line.getNumber(), targetObjectName, annotationsValues);

        log.trace("Built csvAnnotationLine: #{} {} {}",
                  line.getNumber(), targetObjectName, annotationsValues);

        return result;
    }

    /**
     * Options for the CSV content handling policy.
     *
     * Maybe expose these options for client configuration, or provide callbacks
     * for client-side handling, or provide a setLenient/setStrict flag?
     *
     * @author seb
     *
     */
    private static class ReadOptions {

        /** Fail on empty file or skip/keep ? */
        private static final boolean rejectEmptyFile = false;

        /** Fail on empty line or skip/keep ? */
        private static final boolean rejectEmptyLines = false;

        /** Skip empty line or keep ? */
        private static final boolean skipEmptyLines = true;

        /** Fail on malformed (incomplete) line or skip ? */
        private static final boolean rejectMalformedLines = false;

        /** Private constructor */
        private ReadOptions() {
            super();
        }
    }

    /**
     * Constants definitions for the expected CSV file conventional structure.
     *
     * The convention is:
     * - zero-based column indices
     * - name of the annotated type in the first column
     * - starting at the second column, a variable number of columns for
     * the annotation values (one column per annotation, and at least one column)
     *
     * @author seb
     *
     */
    private static class FileStructure {

        /** Column number for the annotated type (ie. first column) */
        private static final int CONTAINER_NAME_RECORD_INDEX = 0;

        /** Column number for the first annotation value (ie. second column) */
        private static final int FIRST_ANNOTATION_RECORD_INDEX = 1;

        /** Minimum number of columns for a given line (ie. at least one annotation value
         * for an annotated type) */
        private static final int MIN_RECORDS_COUNT = 2;

        /** Private constructor */
        private FileStructure() {
            super();
        }
    }

    /**
     * Returns csvStringReader.
     * @return the csvStringReader
     */
    public CsvLineReader<CsvLine> getCsvStringReader() {
        return csvStringReader;
    }

    /**
     * Sets csvStringReader.
     * @param csvStringReader the csvStringReader to set
     */
    public void setCsvStringReader(CsvLineReader<CsvLine> csvStringReader) {
        this.csvStringReader = csvStringReader;
    }

}
