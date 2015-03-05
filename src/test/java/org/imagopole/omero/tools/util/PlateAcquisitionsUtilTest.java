package org.imagopole.omero.tools.util;

import static org.imagopole.omero.tools.TestsUtil.DEFAULT_NAME_PREFIX;
import static org.imagopole.omero.tools.TestsUtil.newPlateAcquisition;
import static org.imagopole.omero.tools.util.PlateAcquisitionsUtil.toPojos;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.Test;

import pojos.PlateAcquisitionData;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class PlateAcquisitionsUtilTest {

    @Test
    public void toPojosShouldConvertNullsToEmptyList() {
        Collection<PojoData> result = toPojos(null);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void toPojosTests() {
        Collection<PlateAcquisitionData> plateAcquisitions = Lists.newArrayList(
                        newPlateAcquisition("pa.name"),
                        newPlateAcquisition(1L),
                        newPlateAcquisition(" "),
                        new PlateAcquisitionData());

        Collection<PojoData> result = toPojos(plateAcquisitions);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 4, "4 results expected");

        PojoData first = Iterables.getFirst(result, null);
        PojoData second = Iterables.get(result, 1, null);
        PojoData third = Iterables.get(result, 2, null);
        PojoData fourth = Iterables.getLast(result, null);

        assertEquals(first.getId(), Long.valueOf(-1L), "Incorrect pojo id");
        assertEquals(first.getName(), "pa.name", "Incorrect pojo name");
        assertNotNull(first.getModelObject(), "Non-null pojo model expected");

        assertEquals(second.getId(), Long.valueOf(+1L), "Incorrect pojo id");
        assertEquals(second.getName(), DEFAULT_NAME_PREFIX + 1, "Incorrect pojo name");
        assertNotNull(second.getModelObject(), "Non-null pojo model expected");

        assertEquals(third.getId(), Long.valueOf(-1L), "Incorrect pojo id");
        assertEquals(third.getName(), "Run -1", "Incorrect pojo name");
        assertNotNull(third.getModelObject(), "Non-null pojo model expected");

        assertEquals(fourth.getId(), Long.valueOf(-1L), "Incorrect pojo id");
        assertEquals(fourth.getName(), "Run -1", "Incorrect pojo name");
        assertNotNull(fourth.getModelObject(), "Non-null pojo model expected");
    }

}
