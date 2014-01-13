package org.imagopole.omero.tools.util;

import static org.imagopole.omero.tools.TestsUtil.DEFAULT_NAME_PREFIX;
import static org.imagopole.omero.tools.TestsUtil.newImage;
import static org.imagopole.omero.tools.util.ImagesUtil.toPojos;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.Test;

import pojos.ImageData;

public class ImagesUtilTest {

    @Test
    public void toPojosShouldConvertNullsToEmptyList() {
        Collection<PojoData> result = toPojos(null);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }

    @Test
    public void toPojosTests() {
        Collection<ImageData> images = Lists.newArrayList(
                        newImage("image.name"),
                        newImage(1L));

        Collection<PojoData> result = toPojos(images);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 2, "2 results expected");

        PojoData first = Iterables.getFirst(result, null);
        PojoData second = Iterables.getLast(result, null);

        assertEquals(first.getId(), Long.valueOf(-1L), "Incorrect pojo id");
        assertEquals(first.getName(), "image.name", "Incorrect pojo name");
        assertNotNull(first.getModelObject(), "Non-null pojo model expected");

        assertEquals(second.getId(), Long.valueOf(+1L), "Incorrect pojo id");
        assertEquals(second.getName(), DEFAULT_NAME_PREFIX + 1, "Incorrect pojo name");
        assertNotNull(second.getModelObject(), "Non-null pojo model expected");
    }

}
