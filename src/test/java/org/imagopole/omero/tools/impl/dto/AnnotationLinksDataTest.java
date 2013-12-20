package org.imagopole.omero.tools.impl.dto;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import omero.model.DatasetAnnotationLinkI;
import omero.model.IObject;

import org.imagopole.omero.tools.api.dto.LinksData;
import org.testng.annotations.Test;


public class AnnotationLinksDataTest {

    @Test
    public void annotationLinksDataShouldAcceptNullContent() {
        LinksData data = AnnotationLinksData.forLinks(null, null);

        assertNull(data.getNewAnnotationLinks(), "Null result expected");
        assertNull(data.getKnownAnnotationLinks(), "Null result expected");
    }

    @Test
    public void annotationLinksDataShouldConvertNullsToEmptyResult() {
        LinksData data = AnnotationLinksData.forLinks(null, null);

        Collection<IObject> result = data.getAllAnnotationLinks();

        assertNotNull(result, "Non-null result expected");
        assertTrue(result.isEmpty(), "Empty results expected");
    }


    @Test
    public void annotationLinksDataTest() {
        List<IObject> expectedKnown =
            Arrays.asList(new IObject[] {
                new DatasetAnnotationLinkI(1L, true) });

        List<IObject> expectedNew =
            Arrays.asList(new IObject[] {
                new DatasetAnnotationLinkI(2L, true),
                new DatasetAnnotationLinkI(3L, true) });

        List<IObject> expectedAll = new ArrayList<IObject>();
        expectedAll.addAll(expectedKnown);
        expectedAll.addAll(expectedNew);

        LinksData data = AnnotationLinksData.forLinks(expectedKnown, expectedNew);

        Collection<IObject> all = data.getAllAnnotationLinks();
        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        Collection<IObject> news = data.getNewAnnotationLinks();

        assertNotNull(all, "Non-null all expected");
        assertNotNull(knowns, "Non-null knowns expected");
        assertNotNull(news, "Non-null news expected");

        assertReflectionEquals("Wrong all results", expectedAll, all, LENIENT_ORDER);
        assertReflectionEquals("Wrong news results", expectedNew, news, LENIENT_ORDER);
        assertReflectionEquals("Wrong knowns results", expectedKnown, knowns, LENIENT_ORDER);
    }

}
