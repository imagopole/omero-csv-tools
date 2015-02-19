package org.imagopole.omero.tools.impl.logic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.model.Project;
import omero.model.ProjectAnnotationLinkI;

import org.imagopole.omero.tools.AbstractBlitzClientTest;
import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.TestsUtil.DbUnit;
import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets;
import org.imagopole.omero.tools.TestsUtil.Groups;
import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.api.blitz.OmeroQueryService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.FileBlitzService;
import org.imagopole.omero.tools.impl.blitz.QueryBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateBlitzService;
import org.imagopole.omero.tools.impl.dto.DefaultAnnotationInfo;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.io.annotation.TempDir;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.ProjectData;

import com.google.common.collect.Iterables;
import com.google.common.io.Files;

/**
 * Note: this test case involves side effects on the OMERO ManagedRepository in use
 *        for integration testing, as the generated original files will persist on the
 *        filesystem afterwards (which may be removed with the OMERO CLI admin tools, via
 *        ./bin/omero admin cleanse /path/to/omero/data/dir).
 */
public class DefaultFileWriterServiceTest extends AbstractBlitzClientTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultFileWriterServiceTest.class);

    /** @TestedObject */
    private DefaultFileWriterService fileWriterService;

    private OmeroFileService blitzFileService;
    private OmeroAnnotationService blitzAnnotationService;

    @TempDir("DefaultFileWriterServiceTest")
    private File tmpOutputDir;

    @Override
    protected void setUpAfterIceConnection(ServiceFactoryPrx session) {
        OmeroFileService blitzFileService = new FileBlitzService(session);
        OmeroQueryService queryService = new QueryBlitzService(session);
        OmeroUpdateService updateService = new UpdateBlitzService(session);
        OmeroAnnotationService annotationService = new AnnotationBlitzService(session);

        fileWriterService = new DefaultFileWriterService();
        fileWriterService.setFileService(blitzFileService);
        fileWriterService.setQueryService(queryService);
        fileWriterService.setUpdateService(updateService);
        fileWriterService.setCharset(Charset.forName("UTF-8"));

        this.blitzFileService = blitzFileService;
        this.blitzAnnotationService = annotationService;
    }

    public File getTmpOutputDir() {
        return tmpOutputDir;
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToPathShouldRejectNullFileName() throws IOException {
        fileWriterService.writeToPath(null, "some.content");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToPathShouldRejectEmptyFileName() throws IOException {
        fileWriterService.writeToPath("  ", "some.content");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToPathShouldRejectNullContent() throws IOException {
        fileWriterService.writeToPath("some.name", null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToPathShouldRejectEmptyContent() throws IOException {
        fileWriterService.writeToPath("some.name", "  ");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = "Invalid file name - file is a directory.*")
    public void writeToPathShouldRejectInvalidFileName() throws IOException {
        fileWriterService.writeToPath(getTmpOutputDir().getAbsolutePath(), "dummy.content");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = "Invalid file path - missing parent directory.*")
    public void writeToPathShouldFailOnMissingPath() throws IOException {
        File missingSubDir = new File(getTmpOutputDir(), "dir.should.not.exist");
        String filePath = new File(missingSubDir, "some.name.txt").getAbsolutePath();

        fileWriterService.writeToPath(filePath, "dummy.content");
    }

    @Test
    public void writeToPathShouldOverwriteExistingFile() throws IOException {
        File outputFile = new File(getTmpOutputDir(), "some.name.txt");

        // create initial file to be overridden
        Files.touch(outputFile);
        String initialContent = Files.toString(outputFile, fileWriterService.getCharset());

        assertNotNull(initialContent, "Non-null content expected");
        assertTrue(initialContent.isEmpty(), "Wrong content");

        // override file
        fileWriterService.writeToPath(outputFile.getAbsolutePath(), "overridden.content");
        String result = Files.toString(outputFile, fileWriterService.getCharset());

        assertNotNull(result, "Non-null results expected");
        assertEquals(result, "overridden.content", "Wrong content");
    }

    @Test
    public void writeToPathTest() throws IOException {
        File outputFile = new File(tmpOutputDir, "some.name.txt");
        fileWriterService.writeToPath(outputFile.getAbsolutePath(), "dummy.content");

        String result = Files.toString(outputFile, fileWriterService.getCharset());

        assertNotNull(result, "Non-null results expected");
        assertEquals(result, "dummy.content", "Wrong content");
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileShouldRejectNullFileName() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFile(null, new MimeType("text/plain"), "some.content");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileShouldRejectEmptyFileName() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFile("  ", new MimeType("text/plain"), "some.content");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileShouldRejectNullMimeType() throws ServerError, IOException {
        fileWriterService.writeToRemoteFile("some.name", null, "some.content");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileShouldRejectNullContent() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFile("some.name", new MimeType("text/plain"), null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileShouldRejectEmptyContent() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFile("some.name", new MimeType("text/plain"), " ");
    }

    @Test
    public void writeToRemoteFileTest() throws ServerError, IOException, MimeTypeParseException {
        Long originalFileId =
            fileWriterService.writeToRemoteFile(
                    "some.name.txt", new MimeType("text/plain"), "dummy.content");

        log.debug("Loading original file (upload only) with id: {}", originalFileId);

        // fetch the uploaded entity for metadata verification
        OriginalFile originalFile =
            (OriginalFile) super.getSession().getQueryService().find(OriginalFile.class.getName(), originalFileId);

        Long expectedSize = new Long("dummy.content".getBytes(fileWriterService.getCharset()).length);
        assertNotNull(originalFile, "Non-null result expected");
        assertEquals(originalFile.getName().getValue(), "some.name.txt", "Wrong file name");
        assertEquals(originalFile.getMimetype().getValue(), "text/plain", "Wrong MIME type");
        assertTrue(originalFile.getPath().getValue().isEmpty(), "Wrong path");
        assertEquals(originalFile.getSize().getValue(), expectedSize.longValue(), "Wrong size");

        // fetch the uploaded file for content verification
        byte[] bytes = blitzFileService.loadOriginalFile(originalFileId, expectedSize);
        String content = new String(bytes, fileWriterService.getCharset());
        assertEquals(content, "dummy.content", "Wrong content");

        // cleanup test side effects: delete the original file entity (but not the FS file - how w/o admin cleanse?)
        super.getSession().getUpdateService().deleteObject(originalFile);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullExperimenter() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            null, 1L, ContainerType.dataset, "some.name",
            new MimeType("text/plain"), "some.content", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullContainerId() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, null, ContainerType.dataset, "some.name",
            new MimeType("text/plain"), "some.content", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullContainerType() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, null, "some.name",
            new MimeType("text/plain"), "some.content", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullFileName() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, ContainerType.dataset, null,
            new MimeType("text/plain"), "some.content", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectEmptyFileName() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, ContainerType.dataset, " ",
            new MimeType("text/plain"), "some.content", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullMimeType() throws ServerError, IOException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, ContainerType.dataset, "some.name",
            null, "some.content", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullContent() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, ContainerType.dataset, "some.name",
            new MimeType("text/plain"), null, DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
           expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectNullAnnotationInfo() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, ContainerType.dataset, "some.name",
            new MimeType("text/plain"), "some.content", null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeToRemoteFileAnnotationShouldRejectEmptyContent() throws ServerError, IOException, MimeTypeParseException {
        fileWriterService.writeToRemoteFileAnnotation(
            1L, 1L, ContainerType.dataset, "some.name",
            new MimeType("text/plain"), "  ", DefaultAnnotationInfo.forInfo("ns", "desc"));
    }

    @DataSet(value = DataSets.Xml.PROJECT_ATTACHMENT_TARGET)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void writeToRemoteFileAnnotationTest() throws ServerError, IOException, MimeTypeParseException {
        Long originalFileId =
            fileWriterService.writeToRemoteFileAnnotation(
                DbUnit.EXPERIMENTER_ID,
                DbUnit.DataSets.Xml.AttachmentTarget.PROJECT_ID,
                ContainerType.project,
                "some.name.txt",
                new MimeType("text/plain"),
                "dummy.content",
                DefaultAnnotationInfo.forInfo("some.ns/export", null));

        log.debug("Loading original file (upload + annotate) with id: {}", originalFileId);

        // basic existence check only - metadata checks already covered by writeToRemoteFileTest()
        OriginalFile originalFile =
            (OriginalFile) super.getSession().getQueryService().find(OriginalFile.class.getName(), originalFileId);
        assertNotNull(originalFile, "Non-null result expected");

        // fetch the annotation for content verification
        Map<Long, Collection<AnnotationData>> result =
            blitzAnnotationService.listAnnotationsLinkedToNodes(
                DbUnit.EXPERIMENTER_ID, Lists.newArrayList(DbUnit.DataSets.Xml.AttachmentTarget.PROJECT_ID),
                Project.class, FileAnnotation.class);

        assertNotNull(result, "Non-null result expected");

        Collection<AnnotationData> annotations = result.get(DbUnit.DataSets.Xml.AttachmentTarget.PROJECT_ID);
        assertNotNull(annotations, "Non-null result expected");
        assertEquals(annotations.size(), 1, "Wrong result count");

        FileAnnotationData annotation = (FileAnnotationData) Iterables.getOnlyElement(annotations);
        assertEquals(annotation.getNameSpace(), "some.ns/export", "Wrong namespace");
        assertNotNull(annotation.getDescription(), "Non-null description expected");
        assertTrue(annotation.getDescription().isEmpty(), "Wrong description");
        assertEquals(annotation.getFileName(), "some.name.txt", "Wrong file name");
        assertEquals(annotation.getOriginalMimetype(), "text/plain", "Wrong MIME type");
        assertTrue(annotation.getFilePath().isEmpty(), "Wrong path");

        //-- cleanup test side effects
        //- drop the project-annotation link
        IObject projectObj =
            super.getSession().getQueryService().find(
                Project.class.getName(), DbUnit.DataSets.Xml.AttachmentTarget.PROJECT_ID);
        ProjectData project = (ProjectData) DataObject.asPojo(projectObj);

        ProjectAnnotationLinkI pal = new ProjectAnnotationLinkI();
        pal.setChild(annotation.asAnnotation());
        pal.setParent(project.asProject());

        IObject link = getSession().getQueryService().findByExample(pal);
        super.getSession().getUpdateService().deleteObject(link);

        //- delete the file annotation
        super.getSession().getUpdateService().deleteObject(annotation.asIObject());

        //- delete the original file entity (but not the FS file - how w/o admin cleanse?)
        super.getSession().getUpdateService().deleteObject(originalFile);
    }

}
