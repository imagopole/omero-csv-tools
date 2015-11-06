package org.imagopole.omero.tools.util;

import static org.imagopole.omero.tools.TestsUtil.DEFAULT_NAME_PREFIX;
import static org.imagopole.omero.tools.TestsUtil.newDataset;
import static org.imagopole.omero.tools.util.DatasetsUtil.toPojos;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import omero.gateway.model.DatasetData;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class DatasetsUtilTest {

    @Test
    public void toPojosShouldConvertNullsToEmptyList() {
        Collection<PojoData> result = toPojos(null);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void toPojosTests() {
        Collection<DatasetData> datasets = Lists.newArrayList(
                        newDataset("dataset.name"),
                        newDataset(1L));

        Collection<PojoData> result = toPojos(datasets);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 2, "2 results expected");

        PojoData first = Iterables.getFirst(result, null);
        PojoData second = Iterables.getLast(result, null);

        assertEquals(first.getId(), Long.valueOf(-1L), "Incorrect pojo id");
        assertEquals(first.getName(), "dataset.name", "Incorrect pojo name");
        assertNotNull(first.getModelObject(), "Non-null pojo model expected");

        assertEquals(second.getId(), Long.valueOf(+1L), "Incorrect pojo id");
        assertEquals(second.getName(), DEFAULT_NAME_PREFIX + 1, "Incorrect pojo name");
        assertNotNull(second.getModelObject(), "Non-null pojo model expected");
    }

}
