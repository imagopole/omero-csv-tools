package org.imagopole.omero.tools.impl.csv;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

public class SimpleAnnotationLineTest {

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationLineNumberShouldBeNonNull() {
        SimpleAnnotationLine.create(null, "non-null", newArrayList("single.element"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationLineNumberLenientShouldBeNonNull() {
        SimpleAnnotationLine.createLenient(null, "non-null", newArrayList("single.element"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationNameShouldBeNonNull() {
        SimpleAnnotationLine.create(1L, null, newArrayList("single.element"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationNameLenientShouldBeNonNull() {
        SimpleAnnotationLine.createLenient(1L, null, newArrayList("single.element"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationNameShouldBeNonEmpty() {
        SimpleAnnotationLine.create(1L, "  ", newArrayList("single.element"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationNameLenientShouldBeNonEmpty() {
        SimpleAnnotationLine.createLenient(1L, "  ", newArrayList("single.element"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationValuesShouldBeBeNonNull() {
        SimpleAnnotationLine.create(1L, "non-null", null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationValuesLenientShouldBeBeNonNull() {
        SimpleAnnotationLine.createLenient(1L, "non-null", null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationLineNumberShouldNotBeZeroIndexed() {
        SimpleAnnotationLine.create(0L, "non-null", new ArrayList<String>());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationLineNumberLenientShouldNotBeZeroIndexed() {
        SimpleAnnotationLine.createLenient(0L, "non-null", new ArrayList<String>());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void csvAnnotationValuesMayNoBeEmpty() {
        SimpleAnnotationLine.create(1L, "non-null", new ArrayList<String>());
    }

    @Test
    public void csvAnnotationValuesLenientMayBeEmpty() {
        CsvAnnotationLine line =
            SimpleAnnotationLine.createLenient(1L, "non-null", new ArrayList<String>());

        assertNotNull(line.getAnnotationsValues(), "Non null annotations expected");
        assertTrue(line.getAnnotationsValues().isEmpty(), "Empty annotations expected");
        assertEquals(line.getAnnotationsSize(), 0, "Zero annotations expected");
    }

    @Test
    public void createTests() {
        CsvAnnotationLine line =
            SimpleAnnotationLine.create(1L, "non-null", newArrayList("single.element"));

        assertNotNull(line.getNumber(), "Non null line number expected");
        assertEquals(line.getNumber().longValue(), 1);

        assertEquals(line.getAnnotatedName(), "non-null");

        assertEquals(line.getAnnotationsSize(), 1);
        assertEquals(line.getSize(), 2);
        assertEquals(Iterables.getOnlyElement(line.getAnnotationsValues()), "single.element");
        assertEquals(line.getValueAt(0), "single.element");
    }

    @Test
    public void createLenientTests() {
        CsvAnnotationLine line =
            SimpleAnnotationLine.createLenient(1L, "non-null", newArrayList("single.element"));

        assertNotNull(line.getNumber(), "Non null line number expected");
        assertEquals(line.getNumber().longValue(), 1);

        assertEquals(line.getAnnotatedName(), "non-null");

        assertEquals(line.getAnnotationsSize(), 1);
        assertEquals(line.getSize(), 2);
        assertEquals(Iterables.getOnlyElement(line.getAnnotationsValues()), "single.element");
        assertEquals(line.getValueAt(0), "single.element");
    }

}
