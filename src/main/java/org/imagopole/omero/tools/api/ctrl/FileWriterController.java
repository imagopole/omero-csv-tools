package org.imagopole.omero.tools.api.ctrl;

import java.io.IOException;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.Args.FileType;
import org.imagopole.omero.tools.api.dto.AnnotationInfo;

/**
 * Dispatcher layer to the file write related services.
 *
 * @author seb
 *
 */
public interface FileWriterController {

    /**
     * Write a CSV file content either to the local filesystem or to an OMERO attached original file.
     *
     * @param experimenterId the experimenter
     * @param containerId the container ID used to locate the remote file
     * @param containerType the type of container used to attach the remote file to
     * @param fileType the type of file (local or remote)
     * @param fileName the file name (may be a path for local files)
     * @param fileContent the file content as String
     * @param annotationInfo the file annotation metadata for remote file types (null for local files)
     * @throws ServerError OMERO client or server failure
     * @throws IOException read failure
     */
    void writeByFileContainerType(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    FileType fileType,
                    String fileName,
                    String fileContent,
                    AnnotationInfo annotationInfo) throws ServerError, IOException;

}
