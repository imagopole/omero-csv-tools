package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;

import omero.ServerError;
import pojos.DatasetData;
import pojos.ImageData;

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
     * @return the datasets
     * @throws ServerError OMERO client or server failure
     */
    Collection<DatasetData> listDatasetsByExperimenterAndProject(
                    Long experimenterId,
                    Long projectId) throws ServerError;

    /**
     * Retrieve all images owned by experimenter within the specified dataset.
     *
     * @param experimenterId the experimenter
     * @param datasetId the dataset
     * @return the images
     * @throws ServerError OMERO client or server failure
     */
    Collection<ImageData> listImagesByExperimenterAndDataset(
                    Long experimenterId,
                    Long datasetId) throws ServerError;

}
