/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Plate;
import omero.model.Project;
import omero.model.Screen;

import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.util.BlitzUtil;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.ShimsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

import com.google.common.collect.Lists;

/**
 * Service layer to the underlying container related OMERO gateway.
 *
 * @author seb
 *
 */
public class ContainersBlitzService implements OmeroContainerService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(ContainersBlitzService.class);

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    /**
     * Parameterized constructor.
     *
     * @param session the OMERO Blitz session
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
            loadContainerHierarchy(experimenterId, projectId, Project.class);

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
     * {@inheritDoc}
     */
     @Override
     @SuppressWarnings("unchecked")
     public Collection<ImageData> listImagesByExperimenterAndContainer(
             Long experimenterId,
             Long containerId,
             Class<? extends IObject> containerClass) throws ServerError {

         Check.notNull(experimenterId, "experimenterId");
         Check.notNull(containerId, "containerId");
         Check.notNull(containerClass, "containerClass");

         Set<ImageData> result = new HashSet<ImageData>();

         List<Image> imageObjects =
            getSession().getContainerService().getImages(
                        containerClass.getName(),
                        Lists.newArrayList(containerId),
                        BlitzUtil.byExperimenter(experimenterId));

         if (null != imageObjects) {

            result = DataObject.asPojos(imageObjects);

         }

         log.debug("Found {} nested images for experimenter {} and container {} of type {}",
                   result.size(), experimenterId, containerId, containerClass.getSimpleName());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<PlateData> listPlatesByExperimenterAndScreen(
            Long experimenterId,
            Long screenId) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(screenId, "screenId");

        Set<PlateData> result = new HashSet<PlateData>();

        List<IObject> plateObjects =
            loadContainerHierarchy(experimenterId, screenId, Screen.class);

         if (null != plateObjects) {

             Set<ScreenData> screens = ShimsUtil.asPojos(plateObjects);

             for (ScreenData screen : screens) {
                 result.addAll(screen.getPlates());
             }

         }

         log.debug("Found {} nested plates for experimenter {} and screen {}",
                   result.size(), experimenterId, screenId);

         return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<PlateAcquisitionData> listPlateAcquisitionsByExperimenterAndPlate(
            Long experimenterId,
            Long plateId) throws ServerError {

         Check.notNull(experimenterId, "experimenterId");
         Check.notNull(plateId, "plateId");

         Set<PlateAcquisitionData> result = new HashSet<PlateAcquisitionData>();

         List<IObject> plateAcquisitionObjects =
             loadContainerHierarchy(experimenterId, plateId, Plate.class);

          if (null != plateAcquisitionObjects) {

              Set<PlateData> plates = ShimsUtil.asPojos(plateAcquisitionObjects);

              for (PlateData plate : plates) {
                  result.addAll(plate.getPlateAcquisitions());
              }

          }

          log.debug("Found {} nested plates for experimenter {} and plate {}",
                    result.size(), experimenterId, plateId);

          return result;
    }

    private List<IObject> loadContainerHierarchy(
            Long experimenterId,
            Long containerId,
            Class<? extends IObject> containerClass) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerClass, "containerClass");

        List<IObject> parentContainerObjects =
             getSession().getContainerService().loadContainerHierarchy(
                     containerClass.getName(),
                     Lists.newArrayList(containerId),
                     BlitzUtil.byExperimenter(experimenterId));

        return parentContainerObjects;
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
