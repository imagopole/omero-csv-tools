package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;

import omero.ServerError;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.model.IObject;

/**
 * Service layer to the underlying container related OMERO gateway.
 *
 * @author seb
 *
 */
public interface OmeroContainerService {

    /**
     * Retrieve all datasets owned by experimenter within the specified project.
     *
     * @param experimenterId the experimenter
     * @param projectId the project
     * @return the datasets, or an empty list if none found
     * @throws ServerError OMERO client or server failure
     */
    Collection<DatasetData> listDatasetsByExperimenterAndProject(
                    Long experimenterId,
                    Long projectId) throws ServerError;

    /**
     * Retrieve all images owned by experimenter within the specified container.
     *
     * @param experimenterId the experimenter
     * @param containerId the container
     * @param containerClass the container type (eg. dataset, plate, plateacquisition)
     * @return the images, or an empty list if none found
     * @throws ServerError OMERO client or server failure
     */
    Collection<ImageData> listImagesByExperimenterAndContainer(
                    Long experimenterId,
                    Long containerId,
                    Class<? extends IObject> containerClass) throws ServerError;

    /**
     * Retrieve all plates owned by experimenter within the specified screen.
     *
     * @param experimenterId the experimenter
     * @param screenId the screen
     * @return the plates, or an empty list if none found
     * @throws ServerError OMERO client or server failure
     */
    Collection<PlateData> listPlatesByExperimenterAndScreen(
                    Long experimenterId,
                    Long screenId) throws ServerError;

    /**
     * Retrieve all plate acquisitions owned by experimenter within the specified plate.
     *
     * @param experimenterId the experimenter
     * @param plateId the plate
     * @return the plate acquisitions, or an empty list if none found
     * @throws ServerError OMERO client or server failure
     */
    Collection<PlateAcquisitionData> listPlateAcquisitionsByExperimenterAndPlate(
                    Long experimenterId,
                    Long plateId) throws ServerError;

}
