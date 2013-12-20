package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;
import java.util.Map;

import omero.ServerError;
import omero.model.IObject;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

public interface OmeroAnnotationService {

    /**
     * @param containerId
     * @return
     * @throws ServerError
     */
    Map<Long, Collection<FileAnnotationData>> listFilesAttachedToContainer(
                    Class<? extends IObject> containerClass,
                    Long containerId) throws ServerError;

    /**
     *
     * @param experimenterId
     * @return
     * @throws ServerError
     */
    Collection<TagAnnotationData> listTagsByExperimenter(Long experimenterId) throws ServerError;

    /**
     * @param experimenterId
     * @param datasetsIds
     * @return
     * @throws ServerError
     */
    Map<Long, Collection<TagAnnotationData>> listTagsLinkedToDatasets(
                    Long experimenterId,
                    Collection<Long> datasetsIds) throws ServerError;

}
