package org.imagopole.omero.tools.api.logic;

import java.io.IOException;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.CsvData;

/**
 * Service layer to the file processing application logic.
 *
 * @author seb
 *
 */
public interface FileReaderService {

    /**
     * Decode a CSV file to String from the filesystem.
     *
     * @param fileName the file name
     * @return the CSV content as String
     * @throws IOException read failure
     */
    CsvData readFromPath(String fileName) throws IOException;

    /**
     * Decode a CSV file to String from a remote OMERO file attachment.
     *
     * @param experimenterId the experimenter
     * @param containerId the container ID used to lookup the attached file from
     * @param containerType the type of container used to locate the attached file (eg. project, dataset)
     * @param fileName the file name
     * @return the CSV content as String
     * @throws ServerError OMERO client or server failure
     * @throws IOException read failure
     */
    CsvData readFromRemoteFileAnnotation(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    String fileName) throws ServerError, IOException;

}
