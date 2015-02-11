package org.imagopole.omero.tools.api.ctrl;

import java.util.Collection;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.PojoData;

/**
 * Dispatcher layer to OMERO containers/metadata services (read-only).
 *
 * @author seb
 *
 */
public interface MetadataController {

    /**
     * Retrieves all OMERO entities owned by experimenter within the specified container, together
     * with their linked annotations if any.
     *
     * @param experimenterId the experimenter
     * @param containerId the container ID
     * @param annotationType the type of annotation to fetch (eg. tag, comment)
     * @param annotatedType the target of the annotation link (eg. dataset, image)
     * @return the model entities, plus any optional related annotations
     * @throws ServerError OMERO client or server failure
     */
    Collection<PojoData> listEntitiesPlusAnnotations(
            Long experimenterId,
            Long containerId,
            AnnotationType annotationType,
            AnnotatedType annotatedType) throws ServerError;

}
