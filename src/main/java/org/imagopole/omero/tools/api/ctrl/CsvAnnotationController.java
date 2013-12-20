package org.imagopole.omero.tools.api.ctrl;

import java.io.IOException;
import java.util.Collection;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.dto.LinksData;

public interface CsvAnnotationController {

    LinksData buildAnnotationsByTypes(
                    Long experimenterId,
                    Long containerId,
                    AnnotationType annotationType,
                    AnnotatedType annotatedType,
                    CsvData csvData) throws ServerError, IOException;

    Collection<IObject> saveAllAnnotationLinks(LinksData linksData) throws ServerError;

}
