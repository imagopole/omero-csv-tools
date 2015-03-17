/**
 *
 */
package org.imagopole.omero.tools.impl.ctrl;

import static org.imagopole.omero.tools.util.AnnotationsUtil.CSV_MIME_TYPE;
import static org.imagopole.omero.tools.util.AnnotationsUtil.EXPORT_DESCRIPTION_FORMAT;
import static org.imagopole.omero.tools.util.AnnotationsUtil.EXPORT_NAMESPACE;
import static org.imagopole.omero.tools.util.ParseUtil.parseContentTypeOrFail;

import java.io.IOException;

import javax.activation.MimeType;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.Args.FileType;
import org.imagopole.omero.tools.api.ctrl.FileWriterController;
import org.imagopole.omero.tools.api.dto.AnnotationInfo;
import org.imagopole.omero.tools.api.logic.FileWriterService;
import org.imagopole.omero.tools.impl.dto.DefaultAnnotationInfo;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher layer to the file write related services.
 *
 * @author seb
 *
 */
public class DefaultFileWriterController implements FileWriterController {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(DefaultFileWriterController.class);

    /** Business logic service abstraction write local/remote files. */
    private FileWriterService fileWriterService;

    /**
     * Vanilla constructor.
     */
    public DefaultFileWriterController() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeByFileContainerType(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            FileType fileType,
            String fileName, String fileContent) throws ServerError, IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(fileType, "fileType");
        Check.notEmpty(fileName, "fileName");
        Check.notEmpty(fileContent, "fileContent");

        switch (fileType) {

            case local:
                writeToLocalFile(experimenterId, fileName, fileContent);
                break;

            case remote:
                writeToRemoteFileAnnotation(
                                experimenterId,
                                containerId,
                                containerType,
                                fileName,
                                fileContent);
                break;

            default:
                throw new UnsupportedOperationException(
                   "File types write support currently limited to local and remote (attachment) only");

        }

        log.debug("Written to: {} file: {}", containerType, fileName);
    }

    private void writeToLocalFile(
            Long experimenterId,
            String fileName,
            String fileContent) throws IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notEmpty(fileName, "fileName");
        Check.notEmpty(fileContent, "fileContent");

        getFileWriterService().writeToPath(fileName, fileContent);
    }

    private Long writeToRemoteFileAnnotation(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            String fileName,
            String fileContent) throws ServerError, IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notEmpty(fileName, "fileName");
        Check.notEmpty(fileContent, "fileContent");

        MimeType contentType = parseContentTypeOrFail(CSV_MIME_TYPE);
        AnnotationInfo annotationInfo = getExportModeAnnotationInfo(containerId, containerType);

        Long result = null;

        // upload and attach to container as file annotation
        switch (containerType) {

            case project:
            case dataset:
            case screen:
            case plate:
            case platerun:
                result = getFileWriterService().writeToRemoteFileAnnotation(
                            experimenterId,
                            containerId,
                            containerType,
                            fileName,
                            contentType,
                            fileContent,
                            annotationInfo);
                break;

            default:
                throw new UnsupportedOperationException(
                    "FileAttachment onto containers other than project/dataset "
                  + "or screen/plate/plateacquisition not supported");
        }

        return result;
    }

    private AnnotationInfo getExportModeAnnotationInfo(Long containerId, ContainerType containerType) {
        String annotationDescription =
            String.format(EXPORT_DESCRIPTION_FORMAT, containerType, containerId);

        return DefaultAnnotationInfo.forInfo(EXPORT_NAMESPACE, annotationDescription);
    }

    /**
     * @return the fileWriterService
     */
    public FileWriterService getFileWriterService() {
        return fileWriterService;
    }

    /**
     * @param fileWriterService the fileWriterService to set
     */
    public void setFileWriterService(FileWriterService fileWriterService) {
        this.fileWriterService = fileWriterService;
    }

}
