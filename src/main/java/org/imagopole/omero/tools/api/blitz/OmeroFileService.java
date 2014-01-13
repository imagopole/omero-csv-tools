package org.imagopole.omero.tools.api.blitz;

import java.io.IOException;

import omero.ServerError;

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

}
