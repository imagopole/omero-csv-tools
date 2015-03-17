/**
 *
 */
package org.imagopole.omero.tools.impl.ctrl;

import java.util.Collection;
import java.util.Collections;

import omero.ServerError;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.ctrl.MetadataController;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.api.logic.MetadataService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher layer to the OMERO containers/metadata related services.
 *
 * @author seb
 *
 */
public class DefaultMetadataController implements MetadataController {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultMetadataController.class);

    /** Query/lookup service for containers/metadata extraction */
    private MetadataService metadataService;

    /**
     * Vanilla constructor
     */
    public DefaultMetadataController() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PojoData> listEntitiesPlusAnnotations(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            AnnotationType annotationType,
            AnnotatedType annotatedType) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");

        Collection<PojoData> result = Collections.emptyList();

        switch(annotationType) {

            case tag:
                result = listEntitiesPlusTags(
                            experimenterId,
                            containerId,
                            containerType,
                            annotationType,
                            annotatedType);
                break;

            default:
                throw new UnsupportedOperationException(
                          "Other annotation types than tags not implemented");

        }

        log.trace(
            "Retrieved {} entities of type {} plus annotations of type {} for experimenter {} and container {}",
            result.size(), annotatedType, annotationType, experimenterId, containerId);

        return result;
    }

    private Collection<PojoData> listEntitiesPlusTags(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    AnnotationType annotationType,
                    AnnotatedType annotatedType) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");

        Collection<PojoData> result = null;

        switch(annotatedType) {

            case dataset:
                result =
                    getMetadataService().listDatasetsPlusAnnotationsByExperimenterAndProject(
                         experimenterId, containerId, annotationType, annotatedType);
                break;

            case image:
                result =
                    getMetadataService().listImagesPlusAnnotationsByExperimenterAndContainer(
                         experimenterId, containerId, containerType, annotationType, annotatedType);
                break;

            case plate:
                result =
                    getMetadataService().listPlatesPlusAnnotationsByExperimenterAndScreen(
                         experimenterId, containerId, annotationType, annotatedType);
                break;

            case platerun:
                result =
                    getMetadataService().listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                         experimenterId, containerId, annotationType, annotatedType);
                break;

            default:
                throw new UnsupportedOperationException(
                    "Listing other containers than datasets/images "
                  + "or plates/plateacquistions not implemented");

        }

        return result;
    }

    /**
     * Returns metadataService.
     * @return the metadataService
     */
    public MetadataService getMetadataService() {
        return metadataService;
    }

    /**
     * Sets metadataService.
     * @param metadataService the metadataService to set
     */
    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

}
