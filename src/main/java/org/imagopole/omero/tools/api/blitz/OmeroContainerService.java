package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;

import omero.ServerError;
import pojos.DatasetData;

public interface OmeroContainerService {

    Collection<DatasetData> listDatasetsByExperimenterAndProject(
                    Long experimenterId,
                    Long projectId) throws ServerError;

}
