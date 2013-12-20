/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.IObject;
import omero.model.Project;

import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.util.BlitzUtil;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/**
 * @author seb
 *
 */
public class ContainersBlitzService implements OmeroContainerService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(ContainersBlitzService.class);

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    /**
     * @param session
     */
    public ContainersBlitzService(ServiceFactoryPrx session) {
        super();

        Check.notNull(session, "session");
        this.session = session;
    }

   /**
    * {@inheritDoc}
    */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<DatasetData> listDatasetsByExperimenterAndProject(
            Long experimenterId,
            Long projectId) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(projectId, "projectId");

        Set<DatasetData> result = new HashSet<DatasetData>();

         List<IObject> projectsObjects =
             getSession().getContainerService().loadContainerHierarchy(
                     Project.class.getName(),
                     Lists.newArrayList(projectId),
                     BlitzUtil.byExperimenter(experimenterId));

         if (null != projectsObjects) {

             Set<ProjectData> projects = DataObject.asPojos(projectsObjects);

             for (ProjectData project : projects) {
                 result.addAll(project.getDatasets());
             }

         }

         log.debug("Found {} nested datasets for experimenter {} and project {}",
                   result.size(), experimenterId, projectId);

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
