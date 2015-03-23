package org.imagopole.omero.tools.impl.csv;

import static org.testng.Assert.assertNotNull;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Collection;
import java.util.List;

import org.imagopole.omero.tools.TestsUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class DefaultCsvHeaderTest {

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getRecordShouldRejectNegativeColumnsParam() {
        DefaultCsvHeader.create().getRecord(-1);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getRecordShouldRejectNullLeadingColumnParam() {
        DefaultCsvHeader.create(null, "some.value");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getRecordShouldRejectEmptyLeadingColumnParam() {
        DefaultCsvHeader.create(" ", "some.value");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getRecordShouldRejectNullStandardColumnParam() {
        DefaultCsvHeader.create("some.value", null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void getRecordShouldRejectEmptyStandardColumnParam() {
        DefaultCsvHeader.create("some.value", " ");
    }

    @Test(dataProvider = "default-header-provider")
    public void defaultHeaderGetRecord(Integer maxColumns, List<String> expected) {
         Collection<String> result = DefaultCsvHeader.create().getRecord(maxColumns);

         assertNotNull(result, "Non null header expected");
         assertReflectionEquals("Incorrect header", expected, result, TestsUtil.DEFAULT_COMPARATOR_MODE);
    }

    @Test(dataProvider = "custom-header-provider")
    public void customHeaderGetRecord(Integer maxColumns, List<String> expected) {
         Collection<String> result = DefaultCsvHeader.create("project", "comment").getRecord(maxColumns);

         assertNotNull(result, "Non null header expected");
         assertReflectionEquals("Incorrect header", expected, result, TestsUtil.DEFAULT_COMPARATOR_MODE);
    }

    @DataProvider(name="default-header-provider")
    private Object[][] provideDefaultHeaderRecords() {
        return new Object[][] {
            { 0, Lists.newArrayList("Entity name")                                 },
            { 1, Lists.newArrayList("Entity name", "Annotation 1")                 },
            { 2, Lists.newArrayList("Entity name", "Annotation 1", "Annotation 2") }
        };
    }

    @DataProvider(name="custom-header-provider")
    private Object[][] provideCustomHeaderRecords() {
        return new Object[][] {
            { 0, Lists.newArrayList("project name")                           },
            { 1, Lists.newArrayList("project name", "comment 1")              },
            { 2, Lists.newArrayList("project name", "comment 1", "comment 2") }
        };
    }

}
