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

    Collection<DatasetData> listDatasetsByExperimenterAndProject(
                    Long experimenterId,
                    Long projectId) throws ServerError;

    Collection<ImageData> listImagesByExperimenterAndDataset(
                    Long experimenterId,
                    Long datasetId) throws ServerError;

}
