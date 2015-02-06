package org.imagopole.omero.tools.api.ctrl;

import java.io.IOException;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;

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
     * @param containerId the container ID used to locate the file (local or remote)
     * @param fileContainerType the type of container used to locate the file (local or remote)
     * @param fileName the file name (may be a path for local files)
     * @param fileContent the file content as String
     * @throws ServerError OMERO client or server failure
     * @throws IOException read failure
     */
    void writeByFileContainerType(
                    Long experimenterId,
                    Long containerId,
                    ContainerType fileContainerType,
                    String fileName,
                    String fileContent) throws ServerError, IOException;

}
