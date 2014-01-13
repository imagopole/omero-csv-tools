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
 * Service layer to the underlying persistence related OMERO gateway.
 *
 * @author seb
 *
 */
public class UpdateBlitzService implements OmeroUpdateService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(UpdateBlitzService.class);

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    /**
     * Parameterized constructor.
     *
     * @param session the OMERO Blitz session
     */
    public UpdateBlitzService(ServiceFactoryPrx session) {
        super();

        Check.notNull(session, "session");
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

            log.info("Persisting {} objects to db", iObjects.size());
            savedObjects = session.getUpdateService().saveAndReturnArray(iObjects);

        } else {
            log.info("No objects to persist - skipping save");
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
