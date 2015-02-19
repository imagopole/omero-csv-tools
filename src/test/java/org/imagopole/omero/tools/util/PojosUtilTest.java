package org.imagopole.omero.tools.util;

import static org.imagopole.omero.tools.TestsUtil.newDataset;
import static org.imagopole.omero.tools.TestsUtil.newDatasetPojo;
import static org.imagopole.omero.tools.TestsUtil.newTag;
import static org.imagopole.omero.tools.impl.csv.SimpleAnnotationLine.createLenient;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import pojos.DatasetData;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;


public class PojosUtilTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(PojosUtilTest.class);

    @Test
    public void indexByNameTests() {
        Collection<DatasetData> datasets = Lists.newArrayList(
                        newDataset("ds.1"), newDataset("ds.1"),
                        newDataset("ds.2"), newDataset("DS.2"));
        Collection<PojoData> pojos = DatasetsUtil.toPojos(datasets);

        Multimap<String, PojoData> result = PojosUtil.indexByName(pojos);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");

        Multiset<String> keysWithDups = result.keys();
        assertReflectionEquals("Wrong keys with dups",
                               Lists.newArrayList("ds.1", "ds.1", "ds.2", "DS.2"),
                               keysWithDups, LENIENT_ORDER);

        Set<String> keys = result.keySet();
        assertReflectionEquals("Wrong keys",
                               Sets.newHashSet("ds.1", "ds.2", "DS.2"),
                               keys, LENIENT_ORDER);

        Collection<PojoData> ds1Values = result.get("ds.1");
        assertNotNull(ds1Values, "Non-null values expected");
        assertReflectionEquals("Wrong values",
                               Lists.newArrayList(newDatasetPojo("ds.1"), newDatasetPojo("ds.1")),
                               ds1Values, LENIENT_ORDER);

        Collection<PojoData> ds2Values = result.get("ds.2");
        assertNotNull(ds1Values, "Non-null values expected");
        assertReflectionEquals("Wrong values",
                               Lists.newArrayList(newDatasetPojo("ds.2")),
                               ds2Values, LENIENT_ORDER);
    }

    @Test
    public void indexByIdTests() {
        Collection<DatasetData> datasets = Lists.newArrayList(
                        newDataset(1L), newDataset(1L),
                        newDataset(2L), newDataset(3L));
        Collection<PojoData> pojos = DatasetsUtil.toPojos(datasets);

        BiMap<Long, PojoData> result = PojosUtil.indexById(pojos);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");

        Set<Long> keys = result.keySet();
        assertReflectionEquals("Wrong keys", Sets.newHashSet(1L, 2L, 3L), keys, LENIENT_ORDER);

        PojoData ds1 = result.get(1L);
        assertNotNull(ds1, "Non-null value expected");

        PojoData ds2 = result.get(2L);
        assertNotNull(ds2, "Non-null value expected");
    }

    @Test
    public void toSortedCsvAnnotationLinesShouldIgnoreNullParams() {
        List<CsvAnnotationLine> result = PojosUtil.toSortedCsvAnnotationLines(null);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void toSortedCsvAnnotationLinesNodesOnlyTests() {
        Collection<DatasetData> datasets = Lists.newArrayList(
                newDataset("ds.z1"), newDataset("ds.a1"),
                newDataset("ds.a2"), newDataset("DS.2"), newDataset("DS.1"));
        Collection<PojoData> pojos = DatasetsUtil.toPojos(datasets);

        List<CsvAnnotationLine> result = PojosUtil.toSortedCsvAnnotationLines(pojos);

        assertNotNull(result, "Non-null results expected");
        log.trace("{}", result);

        List<String> emptyAnnotations =  Collections.emptyList();
        List<CsvAnnotationLine> expected = Lists.newArrayList(
                createLenient(1L, "DS.1", emptyAnnotations),
                createLenient(2L, "DS.2", emptyAnnotations),
                createLenient(3L, "ds.a1", emptyAnnotations),
                createLenient(4L, "ds.a2", emptyAnnotations),
                createLenient(5L, "ds.z1", emptyAnnotations));

        assertReflectionEquals("Wrong ordering", expected, result, TestsUtil.DEFAULT_COMPARATOR_MODE);
    }

    @Test
    public void toSortedCsvAnnotationLinesNodesPlusAnnotationsTests() {
        DatasetData ds1 = newDataset("ds.1");
        ds1.setAnnotations(Sets.newHashSet(newTag("tag.003"), newTag("tag.01")));

        DatasetData ds2 = newDataset("ds.12");
        ds2.setAnnotations(Sets.newHashSet(newTag("Ztag"), newTag("Atag")));

        Collection<DatasetData> datasets = Lists.newArrayList(ds1, ds2);
        Collection<PojoData> pojos = DatasetsUtil.toPojos(datasets);

        List<CsvAnnotationLine> result = PojosUtil.toSortedCsvAnnotationLines(pojos);

        assertNotNull(result, "Non-null results expected");
        log.trace("{}", result);

        List<CsvAnnotationLine> expected = Lists.newArrayList(
                createLenient(1L, "ds.1",  Lists.newArrayList("tag.003", "tag.01")),
                createLenient(2L, "ds.12", Lists.newArrayList("Atag", "Ztag")));

        assertReflectionEquals("Wrong ordering", expected, result, TestsUtil.DEFAULT_COMPARATOR_MODE);
    }

}
