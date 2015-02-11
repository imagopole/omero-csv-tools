package org.imagopole.omero.tools.api.logic;

import java.util.Collection;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.PojoData;

/**
 * Service layer to the OMERO containers/metadata application logic.
 *
 * @author seb
 *
 */
public interface MetadataService {

    /**
     * Retrieves all datasets owned by experimenter within the specified project, together
     * with their linked annotations if any.
     *
     * @param experimenterId the experimenter
     * @param containerId the project ID
     * @param annotationType the type of annotation to fetch (eg. tag, comment)
     * @param annotatedType the target of the annotation link (ie. dataset)
     * @return the datasets, plus any optional related annotations
     * @throws ServerError OMERO client or server failure
     */
    Collection<PojoData> listDatasetsPlusAnnotationsByExperimenterAndProject(
                Long experimenterId,
                Long containerId,
                AnnotationType annotationType,
                AnnotatedType annotatedType) throws ServerError;

    /**
     * Retrieves all images owned by experimenter within the specified dataset, together
     * with their linked annotations if any.
     *
     * @param experimenterId the experimenter
     * @param containerId the dataset ID
     * @param annotationType the type of annotation to fetch (eg. tag, comment)
     * @param annotatedType the target of the annotation link (ie. image)
     * @return the images, plus any optional related annotations
     * @throws ServerError OMERO client or server failure
     */
    Collection<PojoData> listImagesPlusAnnotationsByExperimenterAndDataset(
                Long experimenterId,
                Long containerId,
                AnnotationType annotationType,
                AnnotatedType annotatedType) throws ServerError;

}
