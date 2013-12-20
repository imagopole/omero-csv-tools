package org.imagopole.omero.tools.impl.csv;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import com.google.common.collect.Iterables;

import org.apache.commons.csv.CSVFormat;
import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.csv.CsvLine;
import org.imagopole.omero.tools.api.csv.CsvLineReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CommonsCsvStringReaderTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CommonsCsvStringReaderTest.class);

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void defaultReaderShouldRejectNullContent() {
        CommonsCsvStringReader.defaultReader(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void defaultReaderShouldRejectEmptyContent() {
        CommonsCsvStringReader.defaultReader("    ");
    }

    @Test
    public void defaultReaderFormatTests() {
        CommonsCsvStringReader reader =
            (CommonsCsvStringReader) CommonsCsvStringReader.defaultReader("some.csv.content");

        CSVFormat format = reader.getFormat();

        assertNotNull(format, "Non null format expected");
        assertEquals(Character.valueOf(format.getDelimiter()),
                     Character.valueOf(','), "Wrong delimiter");
        assertFalse(format.getSkipHeaderRecord(), "Wrong skip header setting");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getReaderShouldRejectNullContent() {
        CommonsCsvStringReader.getReader(null, ';', false);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getReaderShouldRejectEmptyContent() {
        CommonsCsvStringReader.getReader("    ", ';', false);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getReaderShouldRejectNullDelim() {
        CommonsCsvStringReader.getReader("some.csv.content", null, false);
    }

    @Test
    public void getReaderFormatTests() {
        CommonsCsvStringReader reader = (CommonsCsvStringReader)
            CommonsCsvStringReader.getReader("some.csv.content", '+', false);

        CSVFormat format = reader.getFormat();

        assertNotNull(format, "Non null format expected");
        assertEquals(Character.valueOf(format.getDelimiter()),
                                       Character.valueOf('+'), "Wrong delimiter");
        assertFalse(format.getSkipHeaderRecord(), "Wrong skip header setting");
    }

    @Test(dataProvider="default-reader-csv-lines-provider")
    public void defaultReaderGetLines(
                    String csvContent,
                    Integer expectedRows,
                    Integer expectedColumnsFirstLine) {

        CsvLineReader<CsvLine> reader =  CommonsCsvStringReader.defaultReader(csvContent);
        Collection<CsvLine> result = reader.getLines();
        log.debug("{} lines", result.size());

        assertNotNull(result,"Non null lines expected");
        assertFalse(result.isEmpty(),"Non empty lines expected");

        CsvLine line = Iterables.getFirst(result, null);
        log.debug("line #{}: {} cols", line.getNumber(), line.getSize());

       assertEquals(Integer.valueOf(result.size()), expectedRows, "Wrong lines count");
       assertEquals(Integer.valueOf(line.getSize()), expectedColumnsFirstLine, "Wrong columns count");
    }

    @Test(dataProvider="custom-delimiter-reader-csv-lines-provider")
    public void getReaderGetLinesWithCustomDelimiter(
                    String csvContent,
                    Integer expectedRows,
                    Integer expectedColumnsFirstLine) {

        CsvLineReader<CsvLine> reader = CommonsCsvStringReader.getReader(csvContent, ';', false);
        Collection<CsvLine> result = reader.getLines();
        log.debug("{} lines", result.size());

        assertNotNull(result,"Non null lines expected");
        assertFalse(result.isEmpty(),"Non empty lines expected");

        CsvLine line = Iterables.getFirst(result, null);
        log.debug("line #{}: {} cols", line.getNumber(), line.getSize());

       assertEquals(Integer.valueOf(result.size()), expectedRows, "Wrong lines count");
       assertEquals(Integer.valueOf(line.getSize()), expectedColumnsFirstLine, "Wrong columns count");
    }

    @Test(dataProvider="custom-delim-skip-header-reader-csv-lines-provider")
    public void getReaderGetLinesWithCustomDelimiterSkipHeader(
                    String csvContent,
                    Integer expectedRows,
                    Integer expectedColumnsFirstLine) {

        CsvLineReader<CsvLine> reader = CommonsCsvStringReader.getReader(csvContent, '|', true);
        Collection<CsvLine> result = reader.getLines();
        log.debug("{} lines", result.size());

        assertNotNull(result,"Non null lines expected");

        CsvLine line = Iterables.getFirst(result, null);

        if (expectedRows == 0) {
            assertTrue(result.isEmpty(),"Empty lines expected");
        } else {
            // check first line
            log.debug("line #{}: {} cols", line.getNumber(), line.getSize());

            assertFalse(result.isEmpty(),"Non empty lines expected");
            assertEquals(Integer.valueOf(result.size()), expectedRows, "Wrong lines count");

            // check columns for first line
            assertEquals(Integer.valueOf(line.getSize()),
                         expectedColumnsFirstLine, "Wrong columns count");
        }
    }

    /**
     * Provides test data and expected results for the default CSV reader.
     * Array structure:
     * - test CSV content
     * - expected number of parsed lines
     * - expected number of columns for the first line
     *
     * @return array of test cases arrays
     */
    @DataProvider(name="default-reader-csv-lines-provider")
    private Object[][] provideDefaultReaderCsvData() {
        return new Object[][] {
            { ",", 1, 2 },
            { ",\n,", 2, 2 },
            { ", \n, container,annot", 2, 2 },
            { "container", 1, 1 },
            { "container,annot", 1, 2 },
            { "container, annot", 1, 2 },
            { "container,\n annot", 2, 2 },
            { "\n container,\n annot \n", 2, 2 },
            { "container,annot.1,annot.2", 1, 3 },
            { "container, \n container,annot", 2, 2 },
            { "container \n container,annot", 2, 1 },
            { "container,annot.1 \n container, annot.2", 2, 2 },
            { "container,annot.1,annot.2 \n container, annot.2", 2, 3 },
            { "container;annot", 1, 1 },
            { "container;annot,annot", 1, 2 },
        };
    }

    /**
     * Provides test data and expected results for a custom CSV reader with
     * a non-default delimiter.
     *
     * Array structure:
     * - test CSV content
     * - expected number of parsed lines
     * - expected number of columns for the first line
     *
     * @return array of test cases arrays
     */
    @DataProvider(name="custom-delimiter-reader-csv-lines-provider")
    private Object[][] provideCustomDelimiterReaderCsvData() {
        return new Object[][] {
            { ";", 1, 2 },
            { ";\n;", 2, 2 },
            { "; \n; container;annot", 2, 2 },
            { "container", 1, 1 },
            { "container;annot", 1, 2 },
            { "container; annot", 1, 2 },
            { "container;\n annot", 2, 2 },
            { "\n container;\n annot \n", 2, 2 },
            { "container;annot.1;annot.2", 1, 3 },
            { "container; \n container;annot", 2, 2 },
            { "container \n container;annot", 2, 1 },
            { "container;annot.1 \n container; annot.2", 2, 2 },
            { "container;annot.1;annot.2 \n container; annot.2", 2, 3 },
            { "container,annot", 1, 1 },
            { "container,annot;annot", 1, 2 },
        };
    }

    /**
     * Provides test data and expected results for a custom CSV reader with
     * a non-default delimiter and ignoring the first line.
     *
     * Array structure:
     * - test CSV content
     * - expected number of parsed lines
     * - expected number of columns for the first line
     *
     * @return array of test cases arrays
     */
    @DataProvider(name="custom-delim-skip-header-reader-csv-lines-provider")
    private Object[][] provideCustomDelimiterSkipHeaderReaderCsvData() {
        return new Object[][] {
            { "|", 0, null }, // null for cols: columns count n/a (no line)
            { "|\n|", 1, 2 },
            { "| \n| container|annot", 1, 3 },
            { "container", 0, null },
            { "container|annot", 0, null },
            { "container| annot", 0, null },
            { "container|\n annot", 1, 1 },
            { "\n container|\n annot \n", 1, 1 },
            { "container|annot.1|annot.2", 0, null },
            { "container| \n container|annot", 1, 2 },
            { "container \n container|annot", 1, 2 },
            { "container|annot.1 \n container| annot.2", 1, 2 },
            { "container|annot.1|annot.2 \n container| annot.2", 1, 2 },
            { "container,annot", 0, null },
            { "container,annot|annot", 0, 0 },
        };
    }
}
