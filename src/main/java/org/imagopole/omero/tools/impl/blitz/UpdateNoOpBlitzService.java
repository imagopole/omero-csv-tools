/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.IObject;

import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op mock implementation of the update service.
 *
 * @author seb
 *
 */
public class UpdateNoOpBlitzService implements OmeroUpdateService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(UpdateNoOpBlitzService.class);

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    /**
     * Parameterized constructor.
     *
     * @param session the OMERO Blitz session
     */
    public UpdateNoOpBlitzService(ServiceFactoryPrx session) {
        super();
        this.session = session;
    }

   /**
    * No-op mock implementation of the update method.
    *
    * @param iObjects the model entities
    * @return an empty list.
    */
    @Override
    public Collection<IObject> saveAll(List<IObject> iObjects) throws ServerError {
        Check.notNull(iObjects, "iObjects");

        List<IObject> savedObjects = Collections.emptyList();

        if (!iObjects.isEmpty()) {
            log.info("No-op [dry-run] - would be persisting {} objects to db", iObjects.size());
        } else {
            log.info("No-op [dry-run] - no objects to persist - would be skipping save");
        }

        return savedObjects;
    }

    /**
     * No-op mock implementation of the update method.
     *
     * @param iObject the model entity
     * @return the argument entity
     */
    @Override
    public IObject save(IObject iObject) throws ServerError {
        Check.notNull(iObject, "iObject");
        log.info("No-op [dry-run] - would be persisting 1 object to db");
        return iObject;
    }

    /**
     * Returns session.
     * @return the session
     */
    public ServiceFactoryPrx getSession() {
        return session;
    }

    /**
     * Sets session.
     * @param session the session to set
     */
    public void setSession(ServiceFactoryPrx session) {
        this.session = session;
    }

}
