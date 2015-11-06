/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omero.ServerError;
import omero.api.IMetadataPrx;
import omero.api.ServiceFactoryPrx;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.model.Dataset;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.OriginalFileI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.util.BlitzUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

import com.google.common.collect.Maps;

/**
 * @author seb
 *
 */
public class AnnotationBlitzServiceTest extends UnitilsTestNG {

//    /** Application logs */
//    private final Logger log = LoggerFactory.getLogger(AnnotationBlitzServiceTest.class);

    /** @TestedObject */
    private AnnotationBlitzService annotationService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    /** FileAnnotationData access */
    private Mock<IMetadataPrx> metadataPrxMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // !deep stubs: the mock actually defining behaviour
        // is the underlying container proxy mock, not the session which acts as pass-through

        // mock the Metadata service on the session mock
        sessionMock.returns(metadataPrxMock.getMock()).getMetadataService();

        // mock the session on the Blitz service mock
        annotationService = new AnnotationBlitzService(sessionMock.getMock());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listFilesAttachedToContainerShouldRejectNullContainerTypeParam() throws ServerError {
        annotationService.listFilesAttachedToContainer(null, 1L);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listFilesAttachedToContainerShouldRejectNullContainerIdParam() throws ServerError {
        annotationService.listFilesAttachedToContainer(ContainerType.project.getModelClass(), null);
    }



    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listAnnotationsLinkedToNodesShouldRejectNullExperimenterIdParam() throws ServerError {
        annotationService.listAnnotationsLinkedToNodes(
            null,
            Lists.newArrayList(1L), ContainerType.project.getModelClass(), AnnotationType.tag.getModelClass());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listAnnotationsLinkedToNodesShouldRejectNullNodesIdsParam() throws ServerError {
        annotationService.listAnnotationsLinkedToNodes(
            1L,
            null,
            ContainerType.project.getModelClass(), AnnotationType.tag.getModelClass());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listAnnotationsLinkedToNodesShouldRejectEmptyNodesIdsParam() throws ServerError {
        annotationService.listAnnotationsLinkedToNodes(
            1L,
            new ArrayList<Long>(),
            ContainerType.project.getModelClass(), AnnotationType.tag.getModelClass());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listAnnotationsLinkedToNodesShouldRejectNullContainerTypeParam() throws ServerError {
        annotationService.listAnnotationsLinkedToNodes(
            1L, Lists.newArrayList(1L),
            null, AnnotationType.tag.getModelClass());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listAnnotationsLinkedToNodesShouldRejectNullNodeTypeParam() throws ServerError {
        annotationService.listAnnotationsLinkedToNodes(
            1L, Lists.newArrayList(1L),
            ContainerType.project.getModelClass(), null);
    }



    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listTagsByExperimenterShouldRejectNullParam() throws ServerError {
        annotationService.listTagsByExperimenter(null);
    }



    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listTagsLinkedToDatasetsShouldRejectNullExpParam() throws ServerError {
        annotationService.listTagsLinkedToContainers(null, Lists.newArrayList(1L), Dataset.class);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listTagsLinkedToDatasetsShouldRejectNullDatasetsParam() throws ServerError {
        annotationService.listTagsLinkedToContainers(1L, null, Dataset.class);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listTagsLinkedToDatasetsShouldRejectEmtyDatasetsParam() throws ServerError {
        annotationService.listTagsLinkedToContainers(1L, new ArrayList<Long>(), Dataset.class);
    }



    @Test
    public void listFilesAttachedToContainerShouldConvertNullsToEmptyResult() throws ServerError {
        metadataPrxMock.returns(null).loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);

        Map<Long, Collection<FileAnnotationData>> result =
            annotationService.listFilesAttachedToContainer(
                ContainerType.project.getModelClass(), 1L);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);
    }

    @Test
    public void listFilesAttachedToContainerShouldReturnEmptyResultWhenNoFileFound() throws ServerError {
        Map<Long, Collection<FileAnnotationData>> expected = Collections.emptyMap();

        metadataPrxMock.returns(expected).loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);

        Map<Long, Collection<FileAnnotationData>> result =
            annotationService.listFilesAttachedToContainer(
                ContainerType.project.getModelClass(), 1L);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);
    }

    @Test
    public void listFilesAttachedToContainer() throws ServerError {
        // fixture
        OriginalFileI originalFile = new OriginalFileI();
        originalFile.setPath(omero.rtypes.rstring("original_file.csv"));
        FileAnnotation fileAnnotation = new FileAnnotationI();
        fileAnnotation.setFile(originalFile);

        Map<Long, Collection<FileAnnotation>> fixture = Maps.newHashMap();
        fixture.put(1L, Lists.newArrayList(fileAnnotation));

        metadataPrxMock.returns(fixture).loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);

        Map<Long, Collection<FileAnnotationData>> result =
            annotationService.listFilesAttachedToContainer(
                ContainerType.project.getModelClass(), 1L);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        Collection<FileAnnotationData> values = result.get(1L);
        FileAnnotationData pojo = getOnlyElement(values);
        assertEquals(pojo.getContentAsString(), "original_file.csv", "Wrong annotation name");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);
    }


    @Test
    public void listAnnotationsLinkedToNodesShouldConvertNullsToEmptyResult() throws ServerError {
        metadataPrxMock.returns(null).loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);

        Map<Long, Collection<AnnotationData>> result =
            annotationService.listAnnotationsLinkedToNodes(
                    1L,
                    Lists.newArrayList(1L),
                    ContainerType.project.getModelClass(),
                    AnnotationType.tag.getModelClass());

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                 TagAnnotation.class.getName(),
                 null,
                 null,
                 ContainerType.project.getModelClass().getName(),
                 Lists.newArrayList(1L),
                 null);
    }

    @Test
    public void listAnnotationsLinkedToNodesShouldReturnEmptyResultWhenNoAnnotationFound() throws ServerError {
        Map<Long, Collection<AnnotationData>> expected = Collections.emptyMap();

        metadataPrxMock.returns(expected).loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);

        Map<Long, Collection<AnnotationData>> result =
            annotationService.listAnnotationsLinkedToNodes(
                    1L,
                    Lists.newArrayList(1L),
                    ContainerType.project.getModelClass(),
                    AnnotationType.tag.getModelClass());

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);
    }

    @Test
    public void listAnnotationsLinkedToNodes() throws ServerError {
        // fixture
        TagAnnotation tagAnnotation = new TagAnnotationI();
        tagAnnotation.setTextValue(omero.rtypes.rstring("tag.annotation"));

        Map<Long, Collection<TagAnnotation>> fixture = Maps.newHashMap();
        fixture.put(1L, Lists.newArrayList(tagAnnotation));

        metadataPrxMock.returns(fixture).loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);

        Map<Long, Collection<AnnotationData>> result =
            annotationService.listAnnotationsLinkedToNodes(
                    1L,
                    Lists.newArrayList(1L),
                    ContainerType.project.getModelClass(),
                    AnnotationType.tag.getModelClass());

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        Collection<AnnotationData> values = result.get(1L);
        AnnotationData pojo = getOnlyElement(values);
        assertEquals(pojo.getContentAsString(), "tag.annotation", "Wrong annotation name");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        ContainerType.project.getModelClass().getName(),
                        Lists.newArrayList(1L),
                        null);
    }


    @Test
    public void listTagsByExperimenterShouldConvertNullsToEmptyResult() throws ServerError {
        metadataPrxMock.returns(null).loadSpecifiedAnnotations(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        BlitzUtil.byExperimenter(1L));

        Collection<TagAnnotationData> result =
            annotationService.listTagsByExperimenter(1L);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotations(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        BlitzUtil.byExperimenter(1L));
    }

    @Test
    public void listTagsByExperimenter() throws ServerError {
        TagAnnotation tag = new TagAnnotationI(1L, true);
        tag.setTextValue(omero.rtypes.rstring("a.text.tag"));

        List<TagAnnotation> fixture = Lists.newArrayList(tag);

        metadataPrxMock.returns(fixture).loadSpecifiedAnnotations(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        BlitzUtil.byExperimenter(1L));

        Collection<TagAnnotationData> result =
            annotationService.listTagsByExperimenter(1L);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        TagAnnotationData pojo = getOnlyElement(result);
        assertEquals(pojo.getContentAsString(), "a.text.tag", "Wrong tag value");
        assertEquals(pojo.getTagValue(), "a.text.tag", "Wrong tag value");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotations(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        BlitzUtil.byExperimenter(1L));
    }

    @Test
    public void listTagsLinkedToDatasetsShouldConvertNullsToEmptyResult() throws ServerError {
        metadataPrxMock.returns(null).loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(1L),
                        BlitzUtil.byExperimenter(1L));

        Map<Long, Collection<TagAnnotationData>> result =
            annotationService.listTagsLinkedToContainers(1L, Lists.newArrayList(1L), Dataset.class);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(1L),
                        BlitzUtil.byExperimenter(1L));
    }

    @Test
    public void listTagsLinkedToDatasets() throws ServerError {
        TagAnnotation tag = new TagAnnotationI(1L, true);
        tag.setTextValue(omero.rtypes.rstring("a.text.tag"));

        Map<Long, Collection<TagAnnotation>> fixture = new HashMap<Long, Collection<TagAnnotation>>();
        fixture.put(1L,  Lists.newArrayList(tag));

        metadataPrxMock.returns(fixture).loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(1L),
                        BlitzUtil.byExperimenter(1L));

        Map<Long, Collection<TagAnnotationData>> result =
            annotationService.listTagsLinkedToContainers(1L, Lists.newArrayList(1L), Dataset.class);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        Collection<TagAnnotationData> values = result.get(1L);
        TagAnnotationData pojo = getOnlyElement(values);
        assertEquals(pojo.getContentAsString(), "a.text.tag", "Wrong tag value");
        assertEquals(pojo.getTagValue(), "a.text.tag", "Wrong tag value");

        sessionMock.assertInvoked().getMetadataService();
        metadataPrxMock.assertInvoked().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(1L),
                        BlitzUtil.byExperimenter(1L));
    }

}
