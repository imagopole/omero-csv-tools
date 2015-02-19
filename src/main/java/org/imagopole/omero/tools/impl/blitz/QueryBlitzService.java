/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.IObject;

import org.imagopole.omero.tools.api.blitz.OmeroQueryService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DataObject;

/**
 * Service layer to the underlying query related OMERO gateway.
 *
 * @author seb
 *
 */
public class QueryBlitzService implements OmeroQueryService {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(QueryBlitzService.class);

    /** OMERO Ice session. */
    private ServiceFactoryPrx session;

    /**
     * Parameterized constructor.
     *
     * @param session the OMERO Blitz session
     */
    public QueryBlitzService(ServiceFactoryPrx session) {
        super();

        Check.notNull(session, "session");
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
     @Override
      public DataObject findById(Class<? extends IObject> entityClass, Long entityId) throws ServerError {
         Check.notNull(entityClass, "entityClass");
         Check.notNull(entityId, "entityId");

         DataObject result = null;

         IObject entityObject = session.getQueryService().find(entityClass.getName(), entityId);

         if (null != entityObject) {

             result = DataObject.asPojo(entityObject);

         }

         log.debug("Found entity with type {} for id {}", entityClass.getName(), entityId);

         return result;
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