package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;
import java.util.Map;

import omero.ServerError;
import omero.model.IObject;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

/**
 * Service layer to the underlying metadata related OMERO gateway.
 *
 * @author seb
 *
 */
public interface OmeroAnnotationService {

    /**
     * Retrieve all file attachments on a given OMERO container, regardless
     * of the owning experimenter.
     *
     * @param containerClass the container model class (eg. <code>omero.model.Project</code>)
     * @param containerId the container ID
     * @return the file attachments, indexed by container
     * @throws ServerError OMERO client or server failure
     */
    Map<Long, Collection<FileAnnotationData>> listFilesAttachedToContainer(
                    Class<? extends IObject> containerClass,
                    Long containerId) throws ServerError;

    /**
     * Retrieve all tags owned by experimenter.
     *
     * @param experimenterId the experimenter
     * @return the tags
     * @throws ServerError OMERO client or server failure
     */
    Collection<TagAnnotationData> listTagsByExperimenter(Long experimenterId) throws ServerError;

    /**
     * Retrieve all tags owned by experimenter and already associated with containers.
     *
     * @param experimenterId the experimenter
     * @param containersIds the container IDs
     * @param containerClass the container model class (eg. <code>omero.model.Project</code>)
     * @return the tags already linked to the requested containers
     * @throws ServerError OMERO client or server failure
     */
    Map<Long, Collection<TagAnnotationData>> listTagsLinkedToContainers(
                    Long experimenterId,
                    Collection<Long> containersIds,
                    Class<? extends IObject> containerClass) throws ServerError;

}
