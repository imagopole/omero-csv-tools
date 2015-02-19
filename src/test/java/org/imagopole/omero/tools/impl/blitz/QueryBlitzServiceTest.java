package org.imagopole.omero.tools.impl.blitz;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Dataset;
import omero.model.DatasetI;

import org.imagopole.omero.tools.TestsUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

import pojos.DataObject;

public class QueryBlitzServiceTest extends UnitilsTestNG {

    /** @TestedObject */
    private QueryBlitzService queryService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    private Mock<IQueryPrx> queryPrxMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // !deep stubs: the mock actually defining behaviour
        // is the underlying container proxy mock, not the session which acts as pass-through

        // mock the Update service on the session mock
        sessionMock.returns(queryPrxMock.getMock()).getQueryService();

        // mock the session on the Blitz service mock
        queryService = new QueryBlitzService(sessionMock.getMock());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void findByIdShouldRejectNullEntityClassParam() throws ServerError {
        queryService.findById(null, 1L);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void findByIdShouldRejectNullEntityIdParam() throws ServerError {
        queryService.findById(Dataset.class, null);
    }

    @Test
    public void findByIdShouldReturnNullWhenNotFound() throws ServerError {
        // fixture behavior
        queryPrxMock.returns(null).find(Dataset.class.getName(), 1L);

        // test
        DataObject result = queryService.findById(Dataset.class, 1L);

        assertNull(result, "Null result expected");

        // check invocations
        sessionMock.assertInvoked().getQueryService();
        queryPrxMock.assertInvoked().find(Dataset.class.getName(), 1L);
    }

    @Test
    public void findByIdKnownDatasetTest() throws ServerError {
         // fixture behavior
        Dataset fixture = new DatasetI(2L, false);
        queryPrxMock.returns(fixture).find(Dataset.class.getName(), 2L);

        // test
        DataObject result = queryService.findById(Dataset.class, 2L);

        assertNotNull(result, "Non null result expected");
        assertEquals(result.getId(), 2L, "Incorrect result id");

        // check invocations
        sessionMock.assertInvoked().getQueryService();
        queryPrxMock.assertInvoked().find(Dataset.class.getName(), 2L);
    }

}
