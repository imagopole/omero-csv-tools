package org.imagopole.omero.tools.impl.csv;

import static org.imagopole.omero.tools.TestsUtil.asListOrNull;
import static org.imagopole.omero.tools.TestsUtil.crlf;
import static org.imagopole.omero.tools.TestsUtil.quote;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.collections.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvLineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;

public class CommonsCsvAnnotationsWriterTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CommonsCsvAnnotationsWriterTest.class);

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getWriterShouldRejectNullDelimiterParam() {
        CommonsCsvAnnotationsWriter.getWriter(null, true);
    }

    @Test(dataProvider="malformed-lines-provider",
          expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void defaultWriterShouldRejectEmptyLinesParam(List<CsvAnnotationLine> lines) {
        CsvLineWriter<CsvAnnotationLine> writer = CommonsCsvAnnotationsWriter.defaultWriter();
        writer.toCsv(lines);
    }

    @Test(dataProvider="malformed-lines-provider",
          expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void customWriterShouldRejectEmptyLinesParam(List<CsvAnnotationLine> lines) {
        CsvLineWriter<CsvAnnotationLine> writer = CommonsCsvAnnotationsWriter.getWriter(';', false);
        writer.toCsv(lines);
    }

    @Test(dataProvider="default-writer-csv-lines-provider")
    public void defaultWriterToCsv(List<CsvAnnotationLine> lines, String expectedCsvContent) {
        CsvLineWriter<CsvAnnotationLine> writer = CommonsCsvAnnotationsWriter.defaultWriter();

        checkCsvContent(writer, lines, expectedCsvContent);
    }

    @Test(dataProvider="semicolon-writer-csv-lines-provider")
    public void customSemicolonWriterToCsv(List<CsvAnnotationLine> lines, String expectedCsvContent) {
        CsvLineWriter<CsvAnnotationLine> writer = CommonsCsvAnnotationsWriter.getWriter(';', false);

        checkCsvContent(writer, lines, expectedCsvContent);
    }

    @Test(dataProvider="pipe-writer-skip-header-csv-lines-provider")
    public void customPipeSkipHeaderWriterToCsv(List<CsvAnnotationLine> lines, String expectedCsvContent) {
        CsvLineWriter<CsvAnnotationLine> writer = CommonsCsvAnnotationsWriter.getWriter('|', true);

        checkCsvContent(writer, lines, expectedCsvContent);
    }

    private void checkCsvContent(
            CsvLineWriter<CsvAnnotationLine> writer,
            List<CsvAnnotationLine> lines,
            String expectedCsvContent) {

        String result = writer.toCsv(lines);
        log.trace("Generated csv: {}", result);

        assertNotNull(result, "Non null content expected");
        assertEquals(result, expectedCsvContent, "Incorrect content");
    }

    private CsvAnnotationLine mockLine(String annotatedName, List<String> annotationsValues) {
         Mock<CsvAnnotationLine> mockLine =
            new MockObject<CsvAnnotationLine>(CsvAnnotationLine.class, null);

         // note: not all return values are set (eg. missing CsvLine interface methods)
         mockLine.returns(annotatedName).getAnnotatedName();
         mockLine.returns(annotationsValues).getAnnotationsValues();

         return mockLine.getMock();
    }

    @DataProvider(name="malformed-lines-provider")
    private Object[][] provideInvalidLines() {
        return new Object[][] {
            { null                                   },
            { Collections.emptyList()                },
            { asListOrNull(mockLine(null, null))     },
            { asListOrNull(mockLine("", null))       },
            { asListOrNull(mockLine(" ", null))      },
            { asListOrNull(mockLine("\n", null))     },
            { asListOrNull(mockLine(" \r\n ", null)) }
        };
    }

    @DataProvider(name="default-writer-csv-lines-provider")
    private Object[][] provideDefaultWriterLines() {
        return new Object[][] {
            { asListOrNull(mockLine(".", null)),      crlf(quote("."))      },
            { asListOrNull(mockLine(",", null)),      crlf(quote(","))      },
            { asListOrNull(mockLine(";", null)),      crlf(quote(";"))      },
            { asListOrNull(mockLine("|", null)),      crlf(quote("|"))      },
            { asListOrNull(mockLine("node", null)),   crlf("node")          },
            { asListOrNull(mockLine(" node ", null)), crlf(quote(" node ")) },
            { asListOrNull(mockLine("node\n", null)), crlf(quote("node\n")) },
            { asListOrNull(mockLine("node.1", null),
                           mockLine("node.2", null)), crlf("node.1").concat(crlf("node.2"))   },
            { asListOrNull(mockLine("node",
                           new ArrayList<String>())), crlf("node")                            },
            { asListOrNull(mockLine("node",
                           newArrayList("annot"))),   crlf("node,annot")                      },
            { asListOrNull(mockLine("node",
                           newArrayList("annot.1", "annot.2"))), crlf("node,annot.1,annot.2") }
        };
    }

    @DataProvider(name="semicolon-writer-csv-lines-provider")
    private Object[][] provideCustomSemicolonWriterLines() {
        return new Object[][] {
            { asListOrNull(mockLine(".", null)),      crlf(quote("."))      },
            { asListOrNull(mockLine(",", null)),      crlf(quote(","))      },
            { asListOrNull(mockLine(";", null)),      crlf(quote(";"))      },
            { asListOrNull(mockLine("|", null)),      crlf(quote("|"))      },
            { asListOrNull(mockLine("node", null)),   crlf("node")          },
            { asListOrNull(mockLine(" node ", null)), crlf(quote(" node ")) },
            { asListOrNull(mockLine("node\n", null)), crlf(quote("node\n")) },
            { asListOrNull(mockLine("node.1", null),
                           mockLine("node.2", null)), crlf("node.1").concat(crlf("node.2"))   },
            { asListOrNull(mockLine("node",
                           new ArrayList<String>())), crlf("node")                            },
            { asListOrNull(mockLine("node",
                           newArrayList("annot"))),   crlf("node;annot")                      },
            { asListOrNull(mockLine("node",
                           newArrayList("annot.1", "annot.2"))), crlf("node;annot.1;annot.2") }
        };
    }

    @DataProvider(name="pipe-writer-skip-header-csv-lines-provider")
    private Object[][] provideCustomPipeSkipHeaderWriterLines() {
        return new Object[][] {
            { asListOrNull(mockLine(".", null)),      crlf(quote("."))      },
            { asListOrNull(mockLine(",", null)),      crlf(quote(","))      },
            { asListOrNull(mockLine(";", null)),      crlf(quote(";"))      },
            { asListOrNull(mockLine("|", null)),      crlf(quote("|"))      },
            { asListOrNull(mockLine("node", null)),   crlf("node")          },
            { asListOrNull(mockLine(" node ", null)), crlf(quote(" node ")) },
            { asListOrNull(mockLine("node\n", null)), crlf(quote("node\n")) },
            { asListOrNull(mockLine("node.1", null),
                           mockLine("node.2", null)), crlf("node.1").concat(crlf("node.2"))   },
            { asListOrNull(mockLine("node",
                           new ArrayList<String>())), crlf("node")                            },
            { asListOrNull(mockLine("node",
                           newArrayList("annot"))),   crlf("node|annot")                      },
            { asListOrNull(mockLine("node",
                           newArrayList("annot.1", "annot.2"))), crlf("node|annot.1|annot.2") }
        };
    }

}
