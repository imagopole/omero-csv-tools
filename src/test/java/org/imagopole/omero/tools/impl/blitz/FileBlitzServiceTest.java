/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import java.io.IOException;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.OriginalFileI;

import org.imagopole.omero.tools.TestsUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

/**
 * @author seb
 *
 */
public class FileBlitzServiceTest extends UnitilsTestNG {

    /** @TestedObject */
    private FileBlitzService fileService;

    /** @InjectInto(property="session") */
    private Mock<ServiceFactoryPrx> sessionMock;

    /** Raw file store access */
    private Mock<RawFileStorePrx> rawFileStorePrxMock;

    @BeforeMethod
    private void setUp() throws ServerError {
        // !deep stubs: the mock actually defining behaviour
        // is the underlying container proxy mock, not the session which acts as pass-through

        // mock the RawFile service on the session mock
        sessionMock.returns(rawFileStorePrxMock.getMock()).createRawFileStore();

        // mock the session on the Blitz service mock
        fileService = new FileBlitzService(sessionMock.getMock());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void loadFromStoreShouldRejectNullFileIdParam() throws ServerError, IOException {
        fileService.loadOriginalFile(null, 100L);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void loadFromStoreShouldRejectNullFileSizeParam() throws ServerError, IOException {
        fileService.loadOriginalFile(1L, null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void loadFromStoreShouldRejectZeroFileSizeParam() throws ServerError, IOException {
        fileService.loadOriginalFile(1L, 0L);
    }

    @Test
    public void loadFromStoreShouldReleaseStatefulResources() throws ServerError, IOException {
        fileService.loadOriginalFile(1L, 100L);

        sessionMock.assertInvoked().createRawFileStore();
        rawFileStorePrxMock.assertInvoked().setFileId(1L);
        rawFileStorePrxMock.assertInvoked().close();
    }

    @Test
    public void loadFromStoreShouldReleaseStatefulResourcesOnError() throws ServerError, IOException {
        rawFileStorePrxMock.returns(new byte[0]).read(0, 4096);
        rawFileStorePrxMock.raises(
            new ServerError(new IllegalArgumentException("mock.download.error"))).read(0, 4096);

        fileService.loadOriginalFile(1L, 5000L);

        sessionMock.assertInvoked().createRawFileStore();
        rawFileStorePrxMock.assertInvoked().setFileId(1L);
        rawFileStorePrxMock.assertInvoked().read(0, 4096);
        rawFileStorePrxMock.assertInvoked().close();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void uploadToStoreShouldRejectNullFileIdParam() throws ServerError, IOException {
        fileService.uploadOriginalFile(null, "file.content".getBytes());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void uploadToStoreShouldRejectNullFileContentParam() throws ServerError, IOException {
        fileService.uploadOriginalFile(1L, null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void uploadToStoreShouldRejectEmptyFileContentParam() throws ServerError, IOException {
        fileService.uploadOriginalFile(1L, "".getBytes());
    }

    @Test
    public void uploadToStoreShouldReleaseStatefulResources() throws ServerError, IOException {
        fileService.uploadOriginalFile(1L, "file.content".getBytes());

        sessionMock.assertInvoked().createRawFileStore();
        rawFileStorePrxMock.assertInvoked().setFileId(1L);
        rawFileStorePrxMock.assertInvoked().close();
    }

    @Test
    public void uploadToStoreShouldReleaseStatefulResourcesOnError() throws ServerError, IOException {
        byte[] fileContent = "file.content".getBytes();
        rawFileStorePrxMock.returns(new OriginalFileI()).save();
        rawFileStorePrxMock.raises(
            new ServerError(new IllegalArgumentException("mock.upload.error"))).save();

        fileService.uploadOriginalFile(1L, fileContent);

        sessionMock.assertInvoked().createRawFileStore();
        rawFileStorePrxMock.assertInvoked().setFileId(1L);
        rawFileStorePrxMock.assertInvoked().write(fileContent, 0L, fileContent.length);
        rawFileStorePrxMock.assertInvoked().save();
        rawFileStorePrxMock.assertInvoked().close();
    }

}
