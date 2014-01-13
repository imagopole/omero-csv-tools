package org.imagopole.omero.tools.api.ctrl;

import java.util.Collection;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.dto.LinksData;

/**
 * Dispatcher layer to the annotation/metadata related services (read-write).
 *
 * @author seb
 *
 */
public interface CsvAnnotationController {

    /**
     * Prepare annotations to be applied. Two steps:
     * - (i) creates tags/annotations if neeeded
     * - (ii) prepares associations, to be persisted as a later step with <code>saveAllAnnotationLinks</code>
     *
     * @param experimenterId the experimenter
     * @param containerId the container ID used to locate the file (local or remote)
     * @param annotationType the type of annotation to use (eg. tag, comment)
     * @param annotatedType the target of the annotation link (eg. dataset, image)
     * @param csvData the CSV content
     * @return
     * @throws ServerError OMERO client or server failure
     */
    LinksData buildAnnotationsByTypes(
                    Long experimenterId,
                    Long containerId,
                    AnnotationType annotationType,
                    AnnotatedType annotatedType,
                    CsvData csvData) throws ServerError;

    /**
     * Persists a collection of OMERO annotation links (ie. tag-entity association).
     *
     * @param linksData the associations to be persisted
     * @return the persisted associations
     * @throws ServerError OMERO client or server failure
     */
    Collection<IObject> saveAllAnnotationLinks(LinksData linksData) throws ServerError;

}
