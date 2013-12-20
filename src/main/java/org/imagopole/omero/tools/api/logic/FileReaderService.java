package org.imagopole.omero.tools.api.logic;

import java.io.IOException;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.CsvData;

public interface FileReaderService {

   CsvData readFromPath(String fileName) throws IOException;

   CsvData readFromRemoteFileAnnotation(
       Long experimenterId,
       Long containerId,
       ContainerType fileContainerType,
       String fileName) throws ServerError, IOException;

}
