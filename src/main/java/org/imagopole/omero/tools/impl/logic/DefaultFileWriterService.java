/**
 *
 */
package org.imagopole.omero.tools.impl.logic;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.activation.MimeType;

import omero.ServerError;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;

import org.imagopole.omero.tools.api.blitz.AnnotationLinker;
import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.api.blitz.OmeroQueryService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.AnnotationInfo;
import org.imagopole.omero.tools.api.logic.FileWriterService;
import org.imagopole.omero.tools.impl.blitz.AnnotationLinkers;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;

import com.google.common.io.Files;

/**
 * Service layer to the file write processing application logic.
 *
 * Note: unlike its <code>FileReaderService</code> counterpart, this service does not perform
 * any validation for multiple files or annotations prior to writing.
 * Hence, multiple annotations with an identical name may be attached to a remote container, and
 * local files will be overwritten.
 *
 * @author seb
 *
 */
public class DefaultFileWriterService implements FileWriterService {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(DefaultFileWriterService.class);

    /** Local Blitz client for FileAnnotationData handling. */
    private OmeroFileService fileService;

    /** Local Blitz client for entities handling. */
    private OmeroQueryService queryService;

    /** Local Blitz client for persistence handling. */
    private OmeroUpdateService updateService;

    /** Charset encoding for CSV bytes conversion. */
    private Charset charset;

    /**
     * Vanilla constructor.
     */
    public DefaultFileWriterService() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToPath(String fileName, String fileContent) throws IOException {
        Check.notEmpty(fileName, "fileName");
        Check.notEmpty(fileContent, "fileContent");

        File file = new File(fileName);

        rejectIfDirectory(file);
        rejectIfParentMissing(file);
        warnIfExists(file);

        log.debug("Writing csv data to local file: {} with charset: {}",
                  file.getAbsolutePath(), getCharset());

        Files.write(fileContent, file, getCharset());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long writeToRemoteFile(
            String fileName,
            MimeType contentType,
            String fileContent) throws ServerError, IOException {

        Check.notEmpty(fileName, "fileName");
        Check.notNull(contentType, "contentType");
        Check.notEmpty(fileContent, "fileContent");

        Long fileId = null;

        byte[] fileBytes = fileContent.getBytes(getCharset());
        int contentLength = fileBytes.length;

        // initialize the remote file handle (OriginalFile object with an identifier for upload)
        OriginalFile originalFile = createOriginalFile(fileName, contentType, contentLength);

        // upload the file contents with the created identifier
        if (null != originalFile) {
            fileId = originalFile.getId().getValue();

            log.debug("Uploading content for original file: {} - {} bytes", fileId, contentLength);
            fileService.uploadOriginalFile(fileId, fileBytes);
        } else {
            log.error("Unable to create remote file handle");
        }

        return fileId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long writeToRemoteFileAnnotation(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            String fileName,
            MimeType contentType,
            String fileContent,
            AnnotationInfo annotationInfo) throws ServerError, IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notEmpty(fileName, "fileName");
        Check.notNull(contentType, "contentType");
        Check.notEmpty(fileContent, "fileContent");
        Check.notNull(annotationInfo, "annotationInfo");

        // upload file content
        Long fileId = writeToRemoteFile(fileName, contentType, fileContent);

        // attach newly created OriginalFile to container
        if (null != fileId) {
            log.debug("Linking original file: {} - {} to container: {} of type {}",
                      fileId, fileName, containerId, containerType);

            attachFileToContainer(fileId, containerType, containerId, annotationInfo);
        }

        return fileId;
    }

    private OriginalFile createOriginalFile(
            String fileName,
            MimeType contentType,
            int contentLength) throws ServerError {

        Check.notEmpty(fileName, "fileName");
        Check.notNull(contentType, "contentType");
        Check.strictlyPositive(contentLength, "contentLength");

        OriginalFile result = null;

        OriginalFile fileObject = new OriginalFileI();
        fileObject.setSize(rlong(contentLength));
        fileObject.setName(rstring(fileName));
        fileObject.setPath(rstring("")); // no server-side path for generated files
        fileObject.setMimetype(rstring(contentType.getBaseType()));

        result = (OriginalFile) updateService.save(fileObject);

        log.debug("Created original file for upload: {} - {} bytes", result.getId().getValue(), contentLength);

        return result;
    }

    private void attachFileToContainer(
            Long fileId,
            ContainerType containerType,
            Long containerId,
            AnnotationInfo annotationInfo) throws ServerError {

        Check.notNull(fileId, "fileId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(annotationInfo, "annotationInfo");

        // lookup annotation target container
        DataObject containerPojo =
            queryService.findById(containerType.getModelClass(), containerId);
        rejectIfNoContainerFound(containerPojo, containerType, containerId);

        // build transient FileAnnotation to be persisted
        AnnotationData fileAnnotationPojo = buildFileAnnotationPojo(fileId, annotationInfo);

        // lookup container-dependent annotation linker and build the container-annotation link
        AnnotationLinker annotationLinker = AnnotationLinkers.forContainerType(containerType);
        IObject linkObject = annotationLinker.link(fileAnnotationPojo, containerPojo);

        // create the FileAnnotation and ContainerAnnotationLink entities
        IObject containerAnnotationLink = updateService.save(linkObject);

        // debug/audit info
        if (null != containerAnnotationLink) {
            Long linkId = containerAnnotationLink.getId().getValue();
            log.debug("Created attachment link: {} for original file: {} and container: {} of type {}",
                      linkId, fileId, containerId, containerType);
        } else {
            log.error("Unable to attach remote file {} to container {} of type {}",
                      fileId, containerId, containerType);
        }
    }

    private AnnotationData buildFileAnnotationPojo(Long fileId, AnnotationInfo annotationInfo) {
        Check.notNull(fileId, "fileId");
        Check.notNull(annotationInfo, "annotationInfo");

        FileAnnotationI annotation = new FileAnnotationI();
        annotation.setFile(new OriginalFileI(fileId, false));

        String ns = annotationInfo.getNamespace();
        if (null != ns && !ns.isEmpty()) {
            annotation.setNs(rstring(ns));
        }

        String desc = annotationInfo.getDescription();
        if (null != desc && !desc.isEmpty()) {
            annotation.setDescription(rstring(desc));
        }

        return new FileAnnotationData(annotation);
    }

    private void rejectIfDirectory(File file) {
        if (null != file && file.isDirectory()) {
            throw new IllegalArgumentException(
                String.format("Invalid file name - file is a directory: %s", file.getAbsolutePath()));
        }
    }

    private void rejectIfParentMissing(File file) {
        if (null != file && null != file.getParentFile() && !file.getParentFile().exists()) {
            throw new IllegalArgumentException(
                String.format("Invalid file path - missing parent directory: %s", file.getAbsolutePath()));
        }
    }

    private void warnIfExists(File file) {
        if (null != file && file.exists()) {
            log.warn("Local file already exists - will be overwritten: {}", file.getAbsolutePath());
        }
    }

    private void rejectIfNoContainerFound(
            DataObject containerPojo,
            ContainerType containerType,
            Long containerId) {

        if (null == containerPojo) {
            throw new IllegalArgumentException(
               String.format("No container of type %s found as attachment target with id: %s",
                             containerType, containerId));
        }
    }

    /**
     * Returns fileAnnotationService.
     * @return the fileAnnotationService
     */
    public OmeroFileService getFileService() {
        return fileService;
    }

    /**
     * Sets fileAnnotationService.
     * @param fileAnnotationService the fileAnnotationService to set
     */
    public void setFileService(OmeroFileService fileAnnotationService) {
        this.fileService = fileAnnotationService;
    }

    /**
     * Returns charset.
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets charset.
     * @param charset the charset to set
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Returns updateService.
     * @return the updateService
     */
    public OmeroUpdateService getUpdateService() {
        return updateService;
    }

    /**
     * Sets updateService.
     * @param updateService the updateService to set
     */
    public void setUpdateService(OmeroUpdateService updateService) {
        this.updateService = updateService;
    }

    /**
     * Returns queryService.
     * @return the queryService
     */
    public OmeroQueryService getQueryService() {
        return queryService;
    }

    /**
     * Sets queryService.
     * @param queryService the queryService to set
     */
    public void setQueryService(OmeroQueryService queryService) {
        this.queryService = queryService;
    }

}
