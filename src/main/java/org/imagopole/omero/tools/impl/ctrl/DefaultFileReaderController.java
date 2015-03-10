/**
 *
 */
package org.imagopole.omero.tools.impl.ctrl;

import java.io.IOException;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.Args.FileType;
import org.imagopole.omero.tools.api.ctrl.FileReaderController;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.logic.FileReaderService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher layer to the file related services.
 *
 * @author seb
 *
 */
public class DefaultFileReaderController implements FileReaderController {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultFileReaderController.class);

    /** Business logic service abstraction read local/remote files */
    private FileReaderService fileReaderService;

    /**
     * Vanilla constructor
     */
    public DefaultFileReaderController() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CsvData readByFileContainerType(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            FileType fileType,
            String fileName) throws ServerError, IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(fileType, "fileType");
        Check.notEmpty(fileName, "fileName");

        CsvData result = null;

        switch (fileType) {

            case local:
                result = readFromLocalFile(experimenterId, fileName);
                break;

            case remote:
                result = readFromRemoteFileAnnotation(
                                experimenterId,
                                containerId,
                                containerType,
                                fileName);
                break;

            default:
                 throw new UnsupportedOperationException(
                    "File types read support currently limited to local and remote (attachment) only");

        }

        log.debug("Reading from {} file: {}", containerType, fileName);

        return result;
    }

    private CsvData readFromLocalFile(
            Long experimenterId,
            String fileName) throws IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notEmpty(fileName, "fileName");

        return getFileReaderService().readFromPath(fileName);
    }

    private CsvData readFromRemoteFileAnnotation(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            String fileName) throws ServerError, IOException {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notEmpty(fileName, "fileName");

        CsvData result = null;

        switch (containerType) {

            case project:
            case dataset:
            case screen:
            case plate:
            case plateacquisition:
                result = getFileReaderService().readFromRemoteFileAnnotation(
                                experimenterId,
                                containerId,
                                containerType,
                                fileName);
                break;

            default:
                throw new UnsupportedOperationException(
                    "FileAttachment lookup on containers other than project/dataset "
                  + "or screen/plate/plateacquisition not supported");
        }

        rejectIfRemoteFileNotFound(containerId, containerType, fileName, result);

        return result;
    }

    private void rejectIfRemoteFileNotFound(
            Long containerId,
            ContainerType containerType,
            String fileName,
            CsvData result) {

        if (null == result) {

            throw new IllegalStateException(String.format(
                "No csv attachment found with name: %1s for container: %d of type: %s",
                fileName, containerId, containerType));

        }
    }

    /**
     * Returns fileReaderService.
     * @return the fileReaderService
     */
    public FileReaderService getFileReaderService() {
        return fileReaderService;
    }

    /**
     * Sets fileReaderService.
     * @param fileReaderService the fileReaderService to set
     */
    public void setFileReaderService(FileReaderService fileService) {
        this.fileReaderService = fileService;
    }

}
