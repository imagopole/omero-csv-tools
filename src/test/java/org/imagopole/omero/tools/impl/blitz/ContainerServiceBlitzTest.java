package org.imagopole.omero.tools.impl.blitz;

import java.util.Collection;
import java.util.List;

import omero.ServerError;
import omero.api.IContainerPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.ImageI;
import omero.model.Project;
import omero.model.ProjectI;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.util.BlitzUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

import pojos.DatasetData;
import pojos.ImageData;

import com.google.common.collect.Lists;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import static com.google.common.collect.Iterables.getOnlyElement;

public class ContainerServiceBlitzTest extends UnitilsTestNG {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(ContainerServiceBlitzTest.class);

    /** @TestedObject */
    private ContainersBlitzService containerService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    private Mock<IContainerPrx> containerPrxMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // !deep stubs: the mock actually defining behaviour
        // is the underlying container proxy mock, not the session which acts as pass-through

        // mock the Container service on the session mock
        sessionMock.returns(containerPrxMock.getMock()).getContainerService();

        // mock the session on the Blitz service mock
        containerService = new ContainersBlitzService(sessionMock.getMock());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listDatasetsByExperimenterAndProjectShouldRejectNullExpParam() throws ServerError {
        containerService.listDatasetsByExperimenterAndProject(null, 1L);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listDatasetsByExperimenterAndProjectShouldRejectNullProjParam() throws ServerError {
        containerService.listDatasetsByExperimenterAndProject(1L, null);
    }

    @Test
    public void listDatasetsByExperimenterAndProjectShouldConvertNullsToEmpty() throws ServerError {
        // fixture behavior
        containerPrxMock.returns(null)
                        .loadContainerHierarchy(
                             Project.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));

        // test
        Collection<DatasetData> result =
            containerService.listDatasetsByExperimenterAndProject(1L, 1L);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        // check invocations
        sessionMock.assertInvoked().getContainerService();
        containerPrxMock.assertInvoked()
                        .loadContainerHierarchy(
                             Project.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }

    @Test
    public void listDatasetsByExperimenterAndProjectWithNoDatasetShouldReturnEmptyResult() throws ServerError {

        // fixture data: a supposedly existing project, with no dataset children
        List<ProjectI> expected = Lists.newArrayList(new ProjectI(1, true));

        // fixture behavior
        containerPrxMock.returns(expected)
                        .loadContainerHierarchy(
                             Project.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));

        // test
        Collection<DatasetData> result =
            containerService.listDatasetsByExperimenterAndProject(1L, 1L);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        // check invocations
        sessionMock.assertInvoked().getContainerService();
        containerPrxMock.assertInvoked()
                        .loadContainerHierarchy(
                             Project.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }

    @Test
    public void listDatasetsByExperimenterAndProjectWithDatasets() throws ServerError {

        // fixture data: a supposedly existing project,
        // with one dataset child
        ProjectI project = new ProjectI(1, true);

        DatasetI dataset = new DatasetI(1, true);
        dataset.setName(omero.rtypes.rstring("dataset.one"));
        project.linkDataset(dataset);

        List<ProjectI> expected = Lists.newArrayList(project);

        // fixture behavior
        containerPrxMock.returns(expected)
                        .loadContainerHierarchy(
                             Project.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));

        // test
        Collection<DatasetData> result =
            containerService.listDatasetsByExperimenterAndProject(1L, 1L);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        DatasetData pojo = getOnlyElement(result);
        assertEquals(pojo.getId(), 1L, "Wrong pojo id");
        assertEquals(pojo.getName(), "dataset.one", "Wrong pojo name");

        // check invocations
        sessionMock.assertInvoked().getContainerService();
        containerPrxMock.assertInvoked()
                        .loadContainerHierarchy(
                             Project.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesByExperimenterAndDatasetShouldRejectNullExpParam() throws ServerError {
        containerService.listImagesByExperimenterAndDataset(null, 1L);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesByExperimenterAndDatasetShouldRejectNullProjParam() throws ServerError {
        containerService.listImagesByExperimenterAndDataset(1L, null);
    }

    @Test
    public void listImagesByExperimenterAndDatasetShouldConvertNullsToEmpty() throws ServerError {
        // fixture behavior
        containerPrxMock.returns(null).getImages(
                             Dataset.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));

        // test
        Collection<ImageData> result =
            containerService.listImagesByExperimenterAndDataset(1L, 1L);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        // check invocations
        sessionMock.assertInvoked().getContainerService();
        containerPrxMock.assertInvoked().getImages(
                             Dataset.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }

    @Test
    public void listImagesByExperimenterAndDatasetsWithImages() throws ServerError {

        // fixture data: a supposedly existing dataset,
        // with one image child
        ImageI image = new ImageI(1, true);
        image.setName(omero.rtypes.rstring("image.one"));

        List<ImageI> expected = Lists.newArrayList(image);

        // fixture behavior
        containerPrxMock.returns(expected).getImages(
                             Dataset.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));

        // test
        Collection<ImageData> result =
            containerService.listImagesByExperimenterAndDataset(1L, 1L);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 1, "1 result expected");

        ImageData pojo = getOnlyElement(result);
        assertEquals(pojo.getId(), 1L, "Wrong pojo id");
        assertEquals(pojo.getName(), "image.one", "Wrong pojo name");

        // check invocations
        sessionMock.assertInvoked().getContainerService();
        containerPrxMock.assertInvoked().getImages(
                             Dataset.class.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }

}
