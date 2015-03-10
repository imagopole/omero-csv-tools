/**
 *
 */
package org.imagopole.omero.tools.impl.logic;

import static org.imagopole.omero.tools.util.AnnotationsUtil.indexByName;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.logic.FileReaderService;
import org.imagopole.omero.tools.impl.dto.DefaultCsvData;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.FileAnnotationData;

/**
 * Service layer to the file processing application logic.
 *
 * @author seb
 *
 */
public class DefaultFileReaderService implements FileReaderService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultFileReaderService.class);

    /** Local Blitz client for FileAnnotationData handling */
    private OmeroFileService fileService;

    /** Local Blitz client for FileAnnotationData handling */
    private OmeroAnnotationService annotationService;

    /** Charset encoding for CSV bytes conversion */
    private Charset charset;

    /**
     * Vanilla constructor
     */
    public DefaultFileReaderService() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CsvData readFromPath(String fileName) throws IOException {
        Check.notEmpty(fileName, "fileName");

        File file = new File(fileName);

        rejectIfNotFoundOrUnreadableLocalFile(file);

        log.debug("Reading csv data from local file: {} with charset: {}",
                  file.getAbsolutePath(), getCharset());

        String fileContent = Files.toString(file, getCharset());

        return DefaultCsvData.forContent(fileContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CsvData readFromRemoteFileAnnotation(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            String fileName) throws ServerError, IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notEmpty(fileName, "fileName");

        // lookup projects which bear the relevant xxx_yyy.csv file with
        // the annotations definitions
        FileAnnotationData attachment =
            findFileAttachmentOrNull(containerId, containerType, fileName);

        CsvData result = null;

        if (null != attachment) {
            result = readFromFileAnnotation(containerId, attachment);
        } else {
            log.warn("Null file annotation data with name {} for container {}", fileName, containerId);
        }

        return result;
    }

    private CsvData readFromFileAnnotation(
            Long containerId, // for audit/debug logs only
            FileAnnotationData attachment) throws ServerError, IOException {

        Check.notNull(containerId, "containerId");
        Check.notNull(attachment, "attachment");

        // avoid possibly dodgy uploads
        rejectIfEmptyOriginalFile(containerId, attachment);

        String fileContent = readAttachment(attachment);

        return DefaultCsvData.forContent(fileContent);
    }

    private String readAttachment(FileAnnotationData attachment)
            throws UnsupportedEncodingException, ServerError, IOException {

        byte[] bytes = loadOriginalFile(attachment);

        log.debug("Converting original file: {} ({}) with encoding: {}",
                   attachment.getFileID(), attachment.getFileName(), getCharset().displayName());

        String fileContent = new String(bytes, getCharset());

        return fileContent;
    }

    private byte[] loadOriginalFile(FileAnnotationData attachment)
                    throws ServerError, IOException {

        long fileSize = attachment.getFileSize();
        long fileId = attachment.getFileID();

        byte[] bytes = fileService.loadOriginalFile(fileId, fileSize);

        log.debug("Loaded csv original file: {} - {} bytes", fileId, bytes.length);

        return bytes;
    }

    private FileAnnotationData findFileAttachmentOrNull(
            Long containerId,
            ContainerType containerType,
            String attachedFileName) throws ServerError {

        FileAnnotationData fileAttachment =
            findFileAnnotationByNameAndContainer(containerId, containerType, attachedFileName);

        log.debug("fileAttachment for container: {} {}", containerId, fileAttachment);

        return fileAttachment;
    }

    // may return null or fail if no attachment found (TBD)
    private FileAnnotationData findFileAnnotationByNameAndContainer(
            Long containerId,
            ContainerType containerType,
            String attachedFileName) throws ServerError {

        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notEmpty(attachedFileName, "attachedFileName");

        FileAnnotationData result = null;

        Class<? extends IObject> containerClass = containerType.getModelClass();
        log.debug("Finding all attachments for container {} of type {}",
                  containerId, containerClass.getSimpleName());

        // retrieve all file attachments for requested container
        Map<Long, Collection<FileAnnotationData>> fileAnnotationsMap =
            getAnnotationService().listFilesAttachedToContainer(containerClass, containerId);

        log.debug("fileAnnotationsMap for container {}: {}", containerId, fileAnnotationsMap);

        if (null != fileAnnotationsMap) {

            // the given container has some attachments
            if (fileAnnotationsMap.containsKey(containerId)) {

                // index those attachments by name
                Collection<FileAnnotationData> fileAnnotations = fileAnnotationsMap.get(containerId);
                Multimap<String, FileAnnotationData> fileAnnotationsByName =
                    indexByName(fileAnnotations);

                // and find the requested one
                if (fileAnnotationsByName.containsKey(attachedFileName)) {
                    Collection<FileAnnotationData> fileAnnotationsWithMatchingName =
                        fileAnnotationsByName.get(attachedFileName);

                    // only one file of the given name must be attached to the container
                    rejectIfDuplicatesAttachments(containerId, fileAnnotationsWithMatchingName);

                    result = Iterables.getOnlyElement(fileAnnotationsWithMatchingName);

                } else {
                    // this container has no file annotation attached
                    // TODO: reject instead? would be more in line with the behaviour below
                    log.warn("No csv file attachment found for container {} with file name {}",
                              containerId, attachedFileName);
                }

            } else {
                // no file attached: fail
                rejectIfNoAttachmentFound(containerId, containerClass);
            }
        }

        return result;
    }

    /**
     * @param containerId
     * @param containerClass
     * @throws IllegalStateException
     */
    private void rejectIfNoAttachmentFound(Long containerId, Class<? extends IObject> containerClass)
                    throws IllegalStateException {

        throw new IllegalStateException(
            String.format("No file attachment returned for container %d of type %s",
                           containerId, containerClass.getSimpleName()));
    }

    /**
     * @param file
     * @throws IllegalStateException
     */
    private void rejectIfNotFoundOrUnreadableLocalFile(File file) throws IllegalStateException {
        if (null != file) {

            if (!file.exists()) {
                throw new IllegalStateException(String.format(
                    "No file found at location: %1s", file.getAbsolutePath()));
            }

            if (!file.canRead()) {
                throw new IllegalStateException(String.format(
                    "File %1$s is not readable",  file.getName()));
            }

        }
    }

    //note: all attachments must be of the same "type" (ie. datasets_tags, or datasets_comments, etc)
    private void rejectIfDuplicatesAttachments(
                    Long containerId,
                    Collection<FileAnnotationData> csvAttachments) throws IllegalStateException {

        if (null != csvAttachments) {

            if (csvAttachments.size() > 1) {
                throw new IllegalStateException(String.format(
                    "Too many csv attachments (%d) for project: %d",
                    csvAttachments.size(), containerId));
            }

        }
    }

    private void rejectIfEmptyOriginalFile(Long containerId, FileAnnotationData csvAttachment)
                    throws IllegalStateException {

        if (null != csvAttachment) {

            if (csvAttachment.getFileSize() == 0) {
                throw new IllegalStateException(String.format(
                    "Zero-length csv attachment (%d) for project: %d",
                    csvAttachment.getId(), containerId));
            }

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
     * Returns structuredAnnotationService.
     * @return the structuredAnnotationService
     */
    public OmeroAnnotationService getAnnotationService() {
        return annotationService;
    }

    /**
     * Sets structuredAnnotationService.
     * @param structuredAnnotationService the structuredAnnotationService to set
     */
    public void setAnnotationService(OmeroAnnotationService structuredAnnotationService) {
        this.annotationService = structuredAnnotationService;
    }

}
