package org.imagopole.omero.tools.impl.logic;

import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;

import org.imagopole.omero.tools.AbstractBlitzClientTest;
import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.TestsUtil.DbUnit;
import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets;
import org.imagopole.omero.tools.TestsUtil.Groups;
import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.FileBlitzService;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.io.annotation.TempFile;

public class DefaultFileReaderServiceTest extends AbstractBlitzClientTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultFileReaderServiceTest.class);

    /** @TestedObject */
    private DefaultFileReaderService fileService;

    @TempFile("unitils_empty_tag.csv")
    private File emptyTagFile;

    @Override
    protected void setUpAfterIceConnection(ServiceFactoryPrx session) {
        OmeroFileService fileAnnotationService = new FileBlitzService(session);
        OmeroAnnotationService structuredAnnotationService = new AnnotationBlitzService(session);

        fileService = new DefaultFileReaderService();
        fileService.setCharset(Charset.forName("UTF-8"));
        fileService.setFileService(fileAnnotationService);
        fileService.setAnnotationService(structuredAnnotationService);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readFromPathShouldRejectNullFileName() throws IOException {
        fileService.readFromPath(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readFromPathShouldRejectEmptyFileName() throws IOException {
        fileService.readFromPath("    ");
    }

    @Test(expectedExceptions = { IllegalStateException.class},
          expectedExceptionsMessageRegExp = "^No file found at location.*")
    public void readFromPathShouldFailIfFileNotFound() throws IOException {
        fileService.readFromPath("some.missing.file.txt");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readFromPathShouldRejectEmptyFile() throws IOException {
        String filePath = this.emptyTagFile.getAbsolutePath();
        log.debug("emptyFilePath: {}", filePath);

        fileService.readFromPath(filePath);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readFromRemoteFileAnnotationShouldRejectNullFileName() throws ServerError, IOException {
        fileService.readFromRemoteFileAnnotation(
            DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, ContainerType.project, null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readFromRemoteFileAnnotationShouldRejectEmptyFileName() throws ServerError, IOException {
        fileService.readFromRemoteFileAnnotation(DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID,
                        ContainerType.project, "    ");
    }

    @DataSet(DataSets.Xml.PROJECT)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class},
          expectedExceptionsMessageRegExp = "^No file attachment.*")
    public void readFromRemoteFileAnnotationShouldFailIfNoAttachmentExistsOnContainer() throws ServerError, IOException {
        fileService.readFromRemoteFileAnnotation(
            DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, ContainerType.project, DbUnit.EMPTY_ORIGINAL_FILE_NAME);
    }

    @DataSet(value= { DataSets.Xml.PROJECT, DataSets.Xml.EMPTY_ATTACHMENT })
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class},
          expectedExceptionsMessageRegExp = "^Zero-length csv attachment.*")
    public void readFromRemoteFileAnnotationShouldFailIfEmptyAttachmentFound() throws ServerError, IOException {
        fileService.readFromRemoteFileAnnotation(
            DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, ContainerType.project, DbUnit.EMPTY_ORIGINAL_FILE_NAME);
    }

    @DataSet(value= { DataSets.Xml.PROJECT, DataSets.Xml.EMPTY_ATTACHMENT, DataSets.Xml.EMPTY_ATTACHMENT_DUPLICATE })
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class},
          expectedExceptionsMessageRegExp = "^Too many csv attachments \\(2\\).*")
    public void readFromRemoteFileAnnotationShouldFailIfDuplicateAttachmentsFound() throws ServerError, IOException {
        fileService.readFromRemoteFileAnnotation(
            DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, ContainerType.project, DbUnit.EMPTY_ORIGINAL_FILE_NAME);
    }

    @DataSet(value= { DataSets.Xml.PROJECT, DataSets.Xml.EMPTY_ATTACHMENT })
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void readFromRemoteFileAnnotationShouldIgnoreAttachmentNotFoundByName() throws ServerError, IOException {
        // FIXME: this is not very consistent with readFromRemoteFileAnnotationShouldFailIfNoAttachmentExistsOnContainer
        CsvData result = fileService.readFromRemoteFileAnnotation(
            DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, ContainerType.project, "some_unknown_file_name.csv");

        assertNull(result, "Null csv data expected");
    }

}
