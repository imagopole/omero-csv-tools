package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;
import java.util.List;

import omero.ServerError;
import omero.model.IObject;

/**
 * Service layer to the underlying persistence related OMERO gateway.
 *
 * @author seb
 *
 */
public interface OmeroUpdateService {

    /**
     * Persists a collection of OMERO model entities.
     *
     * @param iObjects the model entities
     * @return the persisted entities
     * @throws ServerError OMERO client or server failure
     */
    Collection<IObject> saveAll(List<IObject> iObjects) throws ServerError;

}
