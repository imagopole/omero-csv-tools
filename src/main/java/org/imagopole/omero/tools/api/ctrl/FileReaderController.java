package org.imagopole.omero.tools.api.ctrl;

import java.io.IOException;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.CsvData;

public interface FileReaderController {

    CsvData readByFileContainerType(
                    Long experimenterId,
                    Long containerId,
                    ContainerType fileContainerType,
                    String fileName) throws ServerError, IOException;

}
