package org.imagopole.omero.tools.api.blitz;

import omero.ServerError;
import omero.gateway.model.DataObject;
import omero.model.IObject;

/**
 * Service layer to the underlying query related OMERO gateway.
 *
 * @author seb
 *
 */
public interface OmeroQueryService {

    /**
     * Lookup a single OMERO model entity.
     *
     * @param entityClass the model class (eg. <code>omero.model.Project</code>)
     * @param entityId the entity identifier
     * @return the OMERO entity pojo, or raise an exception if not found
     * @throws ServerError OMERO client or server failure
     */
    DataObject findById(Class<? extends IObject> entityClass, Long entityId) throws ServerError;

}
