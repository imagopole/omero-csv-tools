package org.imagopole.omero.tools.api.logic;

import java.io.IOException;

import javax.activation.MimeType;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.AnnotationInfo;

/**
 * Service layer to the file write processing application logic.
 *
 * @author seb
 *
 */
public interface FileWriterService {

    /**
     * Encode a String into a CSV file on the filesystem.
     *
     * @param fileName the file name
     * @param fileContent the CSV content as String
     * @throws IOException read failure
     */
    void writeToPath(String fileName, String fileContent) throws IOException;

    /**
     * Encode a String into a CSV file as a remote OMERO original file.
     *
     * The original file is uploaded, but not linked via a FileAnnotation.
     *
     * @param fileName the file name
     * @param contentType the file MIME type
     * @param fileContent the CSV content as String
     * @return the OMERO OriginalFile ID
     * @throws ServerError OMERO client or server failure
     * @throws IOException read failure
     */
    Long writeToRemoteFile(
            String fileName,
            MimeType contentType,
            String fileContent) throws ServerError, IOException;

    /**
     * Encode a String into a CSV file as a remote OMERO file attachment.
     *
     * The original file is uploaded, then linked to the container via a FileAnnotation.
     *
     * @param experimenterId the experimenter
     * @param containerId the container ID used to lookup the attached file from
     * @param containerType the type of container used to locate the attached file (eg. project, dataset)
     * @param fileName the file name
     * @param contentType the file MIME type
     * @param fileContent the CSV content as String
     * @param annotationInfo the file annotation optional metadata (namespace and description)
     * @return the OMERO OriginalFile ID
     * @throws ServerError OMERO client or server failure
     * @throws IOException read failure
     */
    Long writeToRemoteFileAnnotation(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    String fileName,
                    MimeType contentType,
                    String fileContent,
                    AnnotationInfo annotationInfo) throws ServerError, IOException;

}
