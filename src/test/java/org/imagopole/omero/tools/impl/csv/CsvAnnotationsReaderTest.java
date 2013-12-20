package org.imagopole.omero.tools.impl.csv;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Iterables;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.csv.CsvLine;
import org.imagopole.omero.tools.api.csv.CsvLineReader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;

public class CsvAnnotationsReaderTest {

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void createShouldRejectNullParam() {
       new CsvAnnotationsReader(null);
    }

    @Test
    public void getLinesShouldSkipNullFileContent() {
        Mock<CsvLineReader<CsvLine>> mockReader =
            new MockObject<CsvLineReader<CsvLine>>(CsvLineReader.class, null);

        mockReader.returns(null).getLines();

        CsvAnnotationsReader reader = new CsvAnnotationsReader(mockReader.getMock());

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertTrue(result.isEmpty(),"Empty lines expected");

        mockReader.assertInvoked().getLines();
    }

    @Test
    public void getLinesShouldSkipEmptyFileContent() {
        Mock<CsvLineReader<CsvLine>> mockReader =
            new MockObject<CsvLineReader<CsvLine>>(CsvLineReader.class, null);

        mockReader.returns(Collections.emptyList()).getLines();

        CsvAnnotationsReader reader = new CsvAnnotationsReader(mockReader.getMock());

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertTrue(result.isEmpty(),"Empty lines expected");

        mockReader.assertInvoked().getLines();
    }

    @Test
    public void getLinesShouldSkipNullLines() {
        Mock<CsvLineReader<CsvLine>> mockReader =
            new MockObject<CsvLineReader<CsvLine>>(CsvLineReader.class, null);

        Mock<CsvLine> mockLine = new MockObject<CsvLine>(CsvLine.class, null);
        mockLine.returns(1L).getNumber();
        mockLine.returns(0).getSize();
        mockLine.returns(null).getValueAt(0);

        Collection<CsvLine> nullLinesFixture = Arrays.asList(new CsvLine[] { mockLine.getMock() });

        mockReader.returns(nullLinesFixture).getLines();

        CsvAnnotationsReader reader = new CsvAnnotationsReader(mockReader.getMock());

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertTrue(result.isEmpty(),"Empty lines expected");

        mockReader.assertInvoked().getLines();
        mockLine.assertInvoked().getSize();
        mockLine.assertNotInvoked().getValueAt(0);
    }

    @Test
    public void getLinesShouldSkipEmptyLines() {
        Mock<CsvLineReader<CsvLine>> mockReader =
            new MockObject<CsvLineReader<CsvLine>>(CsvLineReader.class, null);

        Mock<CsvLine> mockLine = new MockObject<CsvLine>(CsvLine.class, null);
        mockLine.returns(1L).getNumber();
        mockLine.returns(0).getSize();
        mockLine.returns("    ").getValueAt(0);

        Collection<CsvLine> emptyLinesFixture = Arrays.asList(new CsvLine[] { mockLine.getMock() });

        mockReader.returns(emptyLinesFixture).getLines();

        CsvAnnotationsReader reader = new CsvAnnotationsReader(mockReader.getMock());

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertTrue(result.isEmpty(),"Empty lines expected");

        mockReader.assertInvoked().getLines();
        mockLine.assertInvoked().getSize();
        mockLine.assertNotInvoked().getValueAt(0);
    }

    @Test(dataProvider="empty-names-provider")
    public void getLinesShouldSkipMalformedLinesWithEmptyTargetName(String invalidTargetName) {
        Mock<CsvLineReader<CsvLine>> mockReader =
            new MockObject<CsvLineReader<CsvLine>>(CsvLineReader.class, null);

        Mock<CsvLine> mockLine = new MockObject<CsvLine>(CsvLine.class, null);
        mockLine.returns(1L).getNumber();
        mockLine.returns(2).getSize();
        mockLine.returns(invalidTargetName).getValueAt(0);
        mockLine.returns("some.annotation").getValueAt(1);

        Collection<CsvLine> malformedLinesFixture =
            Arrays.asList(new CsvLine[] { mockLine.getMock() });

        mockReader.returns(malformedLinesFixture).getLines();

        CsvAnnotationsReader reader = new CsvAnnotationsReader(mockReader.getMock());

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertTrue(result.isEmpty(),"Empty lines expected");

        mockReader.assertInvoked().getLines();
        mockLine.assertInvoked().getSize();
        mockLine.assertInvoked().getValueAt(0);
        mockLine.assertNotInvoked().getValueAt(1);
    }

    @Test
    public void getLinesShouldSkipMalformedLinesWithEmptyAnnotations() {
        Mock<CsvLineReader<CsvLine>> mockReader =
            new MockObject<CsvLineReader<CsvLine>>(CsvLineReader.class, null);

        Mock<CsvLine> mockLineWithNullList = new MockObject<CsvLine>(CsvLine.class, null);
        mockLineWithNullList.returns(1L).getNumber();
        mockLineWithNullList.returns(2).getSize();
        mockLineWithNullList.returns("some.container.name").getValueAt(0);
        mockLineWithNullList.returns(null).getValueAt(1);

        Mock<CsvLine> mockLineWithEmptyList = new MockObject<CsvLine>(CsvLine.class, null);
        mockLineWithEmptyList.returns(2L).getNumber();
        mockLineWithEmptyList.returns(2).getSize();
        mockLineWithEmptyList.returns("some.container.name").getValueAt(0);
        mockLineWithEmptyList.returns("    ").getValueAt(1);

        Collection<CsvLine> malformedLinesFixture =
            Arrays.asList(new CsvLine[] {
                mockLineWithNullList.getMock(), mockLineWithEmptyList.getMock()
            });

        mockReader.returns(malformedLinesFixture).getLines();

        CsvAnnotationsReader reader = new CsvAnnotationsReader(mockReader.getMock());

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertTrue(result.isEmpty(),"Empty lines expected");

        mockReader.assertInvoked().getLines();

        mockLineWithNullList.assertInvoked().getSize();
        mockLineWithNullList.assertInvoked().getValueAt(0);
        mockLineWithNullList.assertInvoked().getValueAt(1);

        mockLineWithEmptyList.assertInvoked().getSize();
        mockLineWithEmptyList.assertInvoked().getValueAt(0);
        mockLineWithEmptyList.assertInvoked().getValueAt(1);
    }

    @Test
    public void getLinesShouldTrimValues() {
        String fixture = "  some.container.name  ,  some.annotation.value , , \n , , , ";
        CsvAnnotationsReader reader =
            new CsvAnnotationsReader(CommonsCsvStringReader.defaultReader(fixture));

        Collection<CsvAnnotationLine> result = reader.getLines();

        assertNotNull(result,"Non null lines expected");
        assertEquals(result.size(), 1, "Wrong lines count");

        CsvAnnotationLine line = Iterables.getOnlyElement(result);
        assertEquals(line.getNumber(), Long.valueOf(1), "Wrong line number");
        assertEquals(line.getSize(), 2, "Wrong line count");
        assertEquals(line.getAnnotationsSize(), 1, "Wrong annotations count");
        assertEquals(line.getAnnotatedName(), "some.container.name", "Wrong target name");
        assertReflectionEquals("Wrong annotation name",
                               Lists.newArrayList("some.annotation.value"),
                               line.getAnnotationsValues(), LENIENT_ORDER);
    }

    @DataProvider(name="empty-names-provider")
    private Object[][] provideEmptyNames() {
        return new Object[][] {
            { null },
            { "" },
            { "    " },
        };
    }

}
