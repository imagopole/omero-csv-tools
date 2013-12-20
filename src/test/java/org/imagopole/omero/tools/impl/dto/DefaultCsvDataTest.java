package org.imagopole.omero.tools.impl.dto;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.testng.annotations.Test;

public class DefaultCsvDataTest {

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvDataShouldRejectNullContent() {
        DefaultCsvData.forContent(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvDataShouldRejectEmptyContent() {
        DefaultCsvData.forContent("    ");
    }

    @Test
    public void csvDataTest() {
        CsvData data = DefaultCsvData.forContent("a.multiline \n content.string \n");

        String result = data.getFileContent();

        assertNotNull(result, "Non-null result expected");
        assertEquals(result, "a.multiline \n content.string \n");
    }

}
