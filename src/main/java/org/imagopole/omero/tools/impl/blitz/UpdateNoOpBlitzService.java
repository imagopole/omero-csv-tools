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
 * @author seb
 *
 */
public class UpdateNoOpBlitzService implements OmeroUpdateService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(UpdateNoOpBlitzService.class);

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    /**
     * @param session
     */
    public UpdateNoOpBlitzService(ServiceFactoryPrx session) {
        super();
        this.session = session;
    }

    /**
     * {@inheritDoc}
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
