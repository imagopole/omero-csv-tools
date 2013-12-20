/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;

import omero.ServerError;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.DatasetAnnotationLinkI;
import omero.model.IObject;

import org.imagopole.omero.tools.TestsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

/**
 * @author seb
 *
 */
public class UpdateBlitzServiceTest extends UnitilsTestNG {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(ContainerServiceBlitzTest.class);

    /** @TestedObject */
    private UpdateBlitzService updateService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    private Mock<IUpdatePrx> updatePrxMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // !deep stubs: the mock actually defining behaviour
        // is the underlying container proxy mock, not the session which acts as pass-through

        // mock the Update service on the session mock
        sessionMock.returns(updatePrxMock.getMock()).getUpdateService();

        // mock the session on the Blitz service mock
        updateService = new UpdateBlitzService(sessionMock.getMock());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveBatchShouldRejectNullParams() throws ServerError {
        updateService.saveAll(null);
    }

    @Test
    public void saveBatchShouldReturnEmptyResultsForEmptyParams() throws ServerError {
        List<IObject> params = Lists.newArrayList();

        Collection<IObject> result = updateService.saveAll(params);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertNotInvoked().getUpdateService();
    }

    @Test
    public void saveBatchShouldReturnConsistenlyWithParams() throws ServerError {
        // expected fixture data
        List<IObject> expected = Arrays.asList(new IObject[] {
            new DatasetAnnotationLinkI(1, true),
            new DatasetAnnotationLinkI(2, true),
        });

        // input fixured data
        List<IObject> params = Arrays.asList(new IObject[] {
            new DatasetAnnotationLinkI(),
            new DatasetAnnotationLinkI(),
        });

        // fixture behavior
        updatePrxMock.returns(expected).saveAndReturnArray(params);

        Collection<IObject> result = updateService.saveAll(params);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");
        assertEquals(result.size(), 2, "2 results expected");
        assertEquals(Iterables.getFirst(result, null).getId().getValue(), 1, "Wrong id returned");

        sessionMock.assertInvoked().getUpdateService();
        updatePrxMock.assertInvoked().saveAndReturnArray(params);
    }

}
