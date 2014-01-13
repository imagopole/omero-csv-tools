package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;
import java.util.Map;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;

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

    Map<Long, Collection<TagAnnotationData>> listTagsLinkedToContainers(
                    Long experimenterId,
                    Collection<Long> containersIds,
                    AnnotatedType annotatedType) throws ServerError;

}
