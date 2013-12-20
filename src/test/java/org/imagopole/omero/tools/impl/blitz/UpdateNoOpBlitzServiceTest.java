/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import omero.ServerError;
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
public class UpdateNoOpBlitzServiceTest extends UnitilsTestNG {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(UpdateNoOpBlitzServiceTest.class);

    /** @TestedObject */
    private UpdateNoOpBlitzService updateService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // mock the session on the Blitz service mock
        updateService = new UpdateNoOpBlitzService(sessionMock.getMock());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveAllShouldRejectNullParams() throws ServerError {
        updateService.saveAll(null);
    }

    @Test
    public void saveAllShouldReturnEmptyResultsForEmptyParams() throws ServerError {
        List<IObject> params = Lists.newArrayList();

        Collection<IObject> result = updateService.saveAll(params);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertNotInvoked().getUpdateService();
    }

    @Test
    public void saveAllShouldReturnIgnoreParams() throws ServerError {
        List<IObject> params = Arrays.asList(new IObject[] {
            new DatasetAnnotationLinkI()
        });

        Collection<IObject> result = updateService.saveAll(params);
        log.debug("{}", result);

        assertNotNull(result, "Non-null results expected");
        assertTrue(result.isEmpty(), "Empty results expected");

        sessionMock.assertNotInvoked().getUpdateService();
    }

}
