package org.imagopole.omero.tools.impl.blitz;

import omero.ServerError;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Dataset;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Project;
import omero.model.Well;

import org.imagopole.omero.tools.util.BlitzUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

import com.google.common.collect.Lists;

public class ShimContainersBlitzServiceTest extends UnitilsTestNG {

     /** @TestedObject */
    private ShimContainersBlitzService containerService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    private Mock<IContainerPrx> containerPrxMock;
    private Mock<IQueryPrx> queryPrxMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // !deep stubs: the mock actually defining behaviour
        // is the underlying container proxy mock, not the session which acts as pass-through

        // mock the Container and Query services on the session mock
        sessionMock.returns(containerPrxMock.getMock()).getContainerService();
        sessionMock.returns(queryPrxMock.getMock()).getQueryService();

        // mock the session on the Blitz service mock
        containerService = new ShimContainersBlitzService(sessionMock.getMock());
    }

    @Test(dataProvider = "pdi-hierarchy-provider")
    @SuppressWarnings( { "rawtypes", "unchecked" })
    public void listImagesByExperimenterAndContainerShouldInvokeDefaultServiceForPDIHierarchy(Class containerClass) throws ServerError {
        // test
        containerService.listImagesByExperimenterAndContainer(1L, 1L, containerClass);

        // check invocations
        sessionMock.assertInvoked().getContainerService();
        sessionMock.assertNotInvoked().getQueryService();

        containerPrxMock.assertInvoked().getImages(
                             containerClass.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }

    @Test(dataProvider = "spwi-hierarchy-provider")
    @SuppressWarnings( { "rawtypes", "unchecked" })
    public void listImagesByExperimenterAndContainerShouldInvokeDefaultServiceForSPWIHierarchy(Class containerClass) throws ServerError {
        // test
        containerService.listImagesByExperimenterAndContainer(1L, 1L, containerClass);

        // check invocations
        sessionMock.assertInvoked().getQueryService();
        sessionMock.assertNotInvoked().getContainerService();

        containerPrxMock.assertNotInvoked().getImages(
                             containerClass.getName(),
                             Lists.newArrayList(1L),
                             BlitzUtil.byExperimenter(1L));
    }

    @DataProvider(name="pdi-hierarchy-provider")
    private Object[][] provideProjectDatasetContainers() {
        return new Object[][] {
            { Project.class },
            { Dataset.class }
        };
    }

    @DataProvider(name="spwi-hierarchy-provider")
    private Object[][] provideScreenPlateContainers() {
        return new Object[][] {
            { Plate.class },
            { PlateAcquisition.class },
            { Well.class }
        };
    }

}
