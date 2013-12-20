package org.imagopole.omero.tools.api.blitz;

import java.io.IOException;

import omero.ServerError;

public interface OmeroFileService {

    byte[] loadOriginalFile(Long fileId, Long fileSize) throws ServerError, IOException;

}
