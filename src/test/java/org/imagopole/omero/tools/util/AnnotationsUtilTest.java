package org.imagopole.omero.tools.util;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.imagopole.omero.tools.TestsUtil.newAttachment;
import static org.imagopole.omero.tools.TestsUtil.newTag;
import static org.imagopole.omero.tools.impl.csv.SimpleAnnotationLine.create;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import omero.model.IObject;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import pojos.AnnotationData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;



public class AnnotationsUtilTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(AnnotationsUtilTest.class);

    @Test
    public void toTagEntitiesShouldConvertNullEmptyResult() {
        List<IObject> result = AnnotationsUtil.toTagEntities(null);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void indexByRowShouldConvertNullEmptyResult() {
        Multimap<String, String> result = AnnotationsUtil.indexByRow(null);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void indexByRowShouldReturnEmpyResultForEmptyList() {
        Collection<CsvAnnotationLine> records = emptyList();
        Multimap<String, String> result = AnnotationsUtil.indexByRow(records);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void indexByRowFroOneLineShouldReturnOneResult() {
        Collection<CsvAnnotationLine> records =
            newArrayList(
                create(1L, "single.key", newArrayList("single.element"))
            );

        Multimap<String, String> result = AnnotationsUtil.indexByRow(records);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        Multiset<String> keysWithDups = result.keys();
        assertReflectionEquals("Wrong keys with dups",
                               Lists.newArrayList("single.key"),
                               keysWithDups, LENIENT_ORDER);

        Set<String> keys = result.keySet();
        assertReflectionEquals("Wrong keys",
                               Lists.newArrayList("single.key"),
                               keys, LENIENT_ORDER);

        Collection<String> values = result.get("single.key");
        assertNotNull(values, "Non-null values expected");
        assertEquals(values.size(), 1, "1 value expected");
        assertReflectionEquals("Wrong values",
                               Lists.newArrayList("single.element"),
                               values, LENIENT_ORDER);
    }

    @Test
    public void indexByRowForOneLineShouldMergeValues() {
        Collection<CsvAnnotationLine> records =
            newArrayList(
                create(1L, "single.key", newArrayList("duplicate.element",
                                                      "duplicate.element"))
            );

        Multimap<String, String> result = AnnotationsUtil.indexByRow(records);
        log.debug("{}", result);


        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 results expected");

        Collection<String> values = result.get("single.key");
        assertNotNull(values, "Non-null values expected");
        assertEquals(values.size(), 1, "1 value expected");
        assertReflectionEquals("Wrong values",
                               Lists.newArrayList("duplicate.element"),
                               values, LENIENT_ORDER);
    }

    @Test
    public void indexByRowShouldMergeKeys() {
        Collection<CsvAnnotationLine> records =
            newArrayList(
                create(1L, "single.key", newArrayList("first.element")),
                create(2L, "single.key", newArrayList("second.element"))
            );

        Multimap<String, String> result = AnnotationsUtil.indexByRow(records);
        log.debug("{}", result);


        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 2, "2 results expected");

        Multiset<String> keysWithDups = result.keys();
        assertReflectionEquals("Wrong keys with dups",
                               Lists.newArrayList("single.key", "single.key"),
                               keysWithDups, LENIENT_ORDER);

        Set<String> keys = result.keySet();
        assertReflectionEquals("Wrong keys",
                               Lists.newArrayList("single.key"),
                               keys, LENIENT_ORDER);

        Collection<String> values = result.get("single.key");
        assertNotNull(values, "Non-null values expected");
        assertEquals(values.size(), 2, "2 values expected");

        List<String> expected = newArrayList("first.element", "second.element");
        assertReflectionEquals("Wrong values", expected, values, LENIENT_ORDER);
    }

    @Test
    public void indexByRowShouldMergeValues() {
        Collection<CsvAnnotationLine> records =
            newArrayList(
                create(1L, "first.key",
                            newArrayList("first.element", "second.element", "duplicate.element")),
                create(2L, "second.key",
                            newArrayList("first.element", "second.element")),
                create(3L, "first.key",
                            newArrayList("third.element", "duplicate.element")),
                create(4L, "second.key",
                            newArrayList("first.element"))
            );

        Multimap<String, String> result = AnnotationsUtil.indexByRow(records);
        log.debug("{}", result);


        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 6, "6 results expected");

         // FIXME check why unitils blarts on duplicate keys with the lenient order
//        Multiset<String> keysWithDups = result.keys();
//        assertReflectionEquals("Wrong keys with dups",
//                               Lists.newArrayList("first.key", "second.key", "first.key", "second.key"),
//                               keysWithDups, LENIENT_ORDER);

        Set<String> keys = result.keySet();
        assertReflectionEquals("Wrong keys",
                               Lists.newArrayList("first.key", "second.key"),
                               keys, LENIENT_ORDER);

        Collection<String> valuesFirstKey = result.get("first.key");
        assertNotNull(valuesFirstKey, "Non-null values expected");
        assertEquals(valuesFirstKey.size(), 4, "4 values expected");

        List<String> expectedFirstKey =
            newArrayList("first.element", "second.element", "third.element", "duplicate.element");
        List<String> actualFirstKey = newArrayList(valuesFirstKey);
        actualFirstKey.removeAll(expectedFirstKey);
        assertTrue(actualFirstKey.isEmpty(), "Identical values expected");


        Collection<String> valuesSecondKey = result.get("second.key");
        assertNotNull(valuesSecondKey, "Non-null values expected");
        assertEquals(valuesSecondKey.size(), 2, "2 values expected");

        List<String> expectedSecondKey =
            newArrayList("first.element", "second.element");
        List<String> actualSecondKey = newArrayList(valuesSecondKey);
        actualSecondKey.removeAll(expectedSecondKey);
        assertTrue(actualSecondKey.isEmpty(), "Identical values expected");
    }

    @Test
    public void indexByFileAnnotationNameTests() {
        Collection<FileAnnotationData> attachments = Lists.newArrayList(
                        newAttachment("file.1"), newAttachment("file.1"),
                        newAttachment("file.2"), newAttachment("FILE.2"));

        Multimap<String, FileAnnotationData> result = AnnotationsUtil.indexByName(attachments);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");

        Multiset<String> keysWithDups = result.keys();
        assertReflectionEquals("Wrong keys with dups",
                               Lists.newArrayList("file.1", "file.1", "file.2", "FILE.2"),
                               keysWithDups, LENIENT_ORDER);

        Set<String> keys = result.keySet();
        assertReflectionEquals("Wrong keys",
                               Sets.newHashSet("file.1", "file.2", "FILE.2"),
                               keys, LENIENT_ORDER);

        Collection<FileAnnotationData> file1Values = result.get("file.1");
        assertNotNull(file1Values, "Non-null values expected");
        assertReflectionEquals("Wrong values",
                               Lists.newArrayList(newAttachment("file.1"), newAttachment("file.1")),
                               file1Values, LENIENT_ORDER);

        Collection<FileAnnotationData> file2Values = result.get("file.2");
        assertNotNull(file1Values, "Non-null values expected");
        assertReflectionEquals("Wrong values",
                               Lists.newArrayList(newAttachment("file.2")),
                               file2Values, LENIENT_ORDER);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void toOrderedAnnotationValuesShouldRejectNullComparatorParam() {
        AnnotationsUtil.toOrderedAnnotationValues(new ArrayList<AnnotationData>(), null);
    }

    @Test
    public void toOrderedAnnotationValuesShouldConvertNullEmptyResult() {
        Comparator<String> naturalOrdering = Ordering.natural();
        List<String> result = AnnotationsUtil.toOrderedAnnotationValues(null, naturalOrdering);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void toOrderedAnnotationValuesTests() {
        Comparator<String> naturalOrdering = Ordering.natural();
        Comparator<String> alphanumOrdering = AnnotationsUtil.EXPORT_LINE_COMPARATOR;

        List<String> emptyResult =
            AnnotationsUtil.toOrderedAnnotationValues(new ArrayList<AnnotationData>(), naturalOrdering);

        assertNotNull(emptyResult, "Non-null results expected");
        assertTrue(emptyResult.isEmpty(), "Empty results expected");

        List<TagAnnotationData> tags = Lists.newArrayList(
                newTag("tag.z1"), newTag("TAG.z1"), newTag("TAG.z10"),
                newTag("tag.a2"), newTag("tag.a1"),
                newTag("das_02"), newTag("Des"), newTag("das_01"),
                newTag("tag.2"), newTag("tag.1"), newTag("tag.11"),
                newTag("TAG.2"), newTag("TAG.1"));

        List<String> resultDefaultOrder = AnnotationsUtil.toOrderedAnnotationValues(tags, naturalOrdering);
        List<String> resultAlphanumOrder = AnnotationsUtil.toOrderedAnnotationValues(tags, alphanumOrdering);

        assertNotNull(resultDefaultOrder, "Non-null results expected");
        assertNotNull(resultAlphanumOrder, "Non-null results expected");
        log.trace("default: {}", resultDefaultOrder);
        log.trace("alphanum: {}", resultAlphanumOrder);

        List<String> expectedDefaultOrder = Lists.newArrayList(
           "Des", "TAG.1", "TAG.2", "TAG.z1", "TAG.z10",  "das_01", "das_02", "tag.1", "tag.11", "tag.2", "tag.a1", "tag.a2", "tag.z1");

        List<String> expectedAlphanumOrder = Lists.newArrayList(
            "das_01", "das_02", "Des", "tag.1", "TAG.1", "tag.2", "TAG.2", "tag.11", "tag.a1", "tag.a2", "tag.z1", "TAG.z1", "TAG.z10");

        assertReflectionEquals("Wrong default ordering", expectedDefaultOrder, resultDefaultOrder, TestsUtil.DEFAULT_COMPARATOR_MODE);
        assertReflectionEquals("Wrong alphanum ordering", expectedAlphanumOrder, resultAlphanumOrder, TestsUtil.DEFAULT_COMPARATOR_MODE);
    }

}
