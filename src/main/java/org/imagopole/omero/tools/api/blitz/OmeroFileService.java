package org.imagopole.omero.tools.api.blitz;

import java.io.IOException;

import omero.ServerError;
import omero.model.OriginalFile;

/**
 * Service layer to the underlying file related OMERO gateway.
 *
 * @author seb
 *
 */
public interface OmeroFileService {

    /**
     * Retrieve the file content (typically a file attachment) from the remote OMERO file store.
     *
     * @param fileId the original file ID
     * @param fileSize the original file size
     * @return the raw file bytes
     * @throws ServerError OMERO client or server failure
     * @throws IOException read failure
     */
    byte[] loadOriginalFile(Long fileId, Long fileSize) throws ServerError, IOException;

    /**
     * Upload the file content to the remote OMERO file store.
     *
     * @param fileId the original file ID
     * @param fileContent the raw file bytes
     * @return the OriginalFile handle to the uploaded file
     * @throws ServerError OMERO client or server failure
     * @throws IOException write failure
     */
    OriginalFile uploadOriginalFile(Long fileId, byte[] fileContent) throws ServerError, IOException;

}
