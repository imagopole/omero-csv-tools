/**
 *
 */
package org.imagopole.omero.tools.impl.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import omero.ServerError;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;

import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.api.logic.MetadataService;
import org.imagopole.omero.tools.impl.dto.DefaultPojoData;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.DatasetsUtil;
import org.imagopole.omero.tools.util.FunctionsUtil;
import org.imagopole.omero.tools.util.ImagesUtil;
import org.imagopole.omero.tools.util.PlateAcquisitionsUtil;
import org.imagopole.omero.tools.util.PlatesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;

/**
 * Service layer to the OMERO containers/metadata application logic.
 *
 * @author seb
 *
 */
public class DefaultMetadataService implements MetadataService {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(DefaultMetadataService.class);

    /** Local Blitz client for container handling. */
    private OmeroContainerService containerService;

    /** Local Blitz client for AnnotationData handling. */
    private OmeroAnnotationService annotationService;

    /**
     * Default constructor.
     */
    public DefaultMetadataService() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PojoData> listDatasetsPlusAnnotationsByExperimenterAndProject(
            Long experimenterId,
            Long containerId,
            AnnotationType annotationType,
            AnnotatedType annotatedType) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");

        // -- (i) lookup model objects: datasets *within* the given project
        Collection<DatasetData> experimenterDatasets =
            getContainerService().listDatasetsByExperimenterAndProject(experimenterId, containerId);

        // -- wrap omero model entities into an internal representation for processing
        Collection<PojoData> experimenterPojos = DatasetsUtil.toPojos(experimenterDatasets);

        // -- no dataset found - no further processing
        rejectIfEmpty(ContainerType.project, annotatedType, containerId, experimenterPojos);

        // -- (ii) common processing: lookup tags/annotations linked to the pojos
        Collection<PojoData> result =
            loadLinkedPojosAnnotations(experimenterPojos, experimenterId, annotationType, annotatedType);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PojoData> listImagesPlusAnnotationsByExperimenterAndContainer(
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

        // -- (i) lookup model objects: images *within* the given container
        Collection<ImageData> experimenterImages =
            getContainerService().listImagesByExperimenterAndContainer(
                    experimenterId,
                    containerId,
                    containerType.getModelClass());

        // -- wrap omero model entities into an internal representation for processing
        Collection<PojoData> experimenterPojos = ImagesUtil.toPojos(experimenterImages);

        // -- no image found - no further processing
        rejectIfEmpty(containerType, annotatedType, containerId, experimenterPojos);

       // -- (ii) common processing: lookup tags/annotations linked to the pojos
        Collection<PojoData> result =
            loadLinkedPojosAnnotations(experimenterPojos, experimenterId, annotationType, annotatedType);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PojoData> listPlatesPlusAnnotationsByExperimenterAndScreen(
            Long experimenterId,
            Long containerId,
            AnnotationType annotationType,
            AnnotatedType annotatedType) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");

        // -- (i) lookup model objects: plates *within* the given screen
        Collection<PlateData> experimenterPlates =
            getContainerService().listPlatesByExperimenterAndScreen(
                    experimenterId,
                    containerId);

        // -- wrap omero model entities into an internal representation for processing
        Collection<PojoData> experimenterPojos = PlatesUtil.toPojos(experimenterPlates);

        // -- no plate found - no further processing
        rejectIfEmpty(ContainerType.screen, annotatedType, containerId, experimenterPojos);

       // -- (ii) common processing: lookup tags/annotations linked to the pojos
        Collection<PojoData> result =
            loadLinkedPojosAnnotations(experimenterPojos, experimenterId, annotationType, annotatedType);

        return result;
    }

    @Override
    public Collection<PojoData> listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
            Long experimenterId,
            Long containerId,
            AnnotationType annotationType,
            AnnotatedType annotatedType) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");

        // -- (i) lookup model objects: plateacquisitions *within* the given plate
        Collection<PlateAcquisitionData> experimenterPlateRuns =
            getContainerService().listPlateAcquisitionsByExperimenterAndPlate(
                    experimenterId,
                    containerId);

        // -- wrap omero model entities into an internal representation for processing
        Collection<PojoData> experimenterPojos = PlateAcquisitionsUtil.toPojos(experimenterPlateRuns);

        // -- no plateacquisition found - no further processing
        rejectIfEmpty(ContainerType.plate, annotatedType, containerId, experimenterPojos);

       // -- (ii) common processing: lookup tags/annotations linked to the pojos
        Collection<PojoData> result =
            loadLinkedPojosAnnotations(experimenterPojos, experimenterId, annotationType, annotatedType);

        return result;
    }

    protected Collection<PojoData> loadLinkedPojosAnnotations(
            Collection<PojoData> experimenterPojos,
            Long experimenterId,
            AnnotationType annotationType,
            AnnotatedType annotatedType) throws ServerError {

        Check.notEmpty(experimenterPojos, "experimenterPojos");
        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");

        Collection<PojoData> result = new ArrayList<PojoData>(experimenterPojos.size());

        // -- (i) extract the requested pojos IDs
        Collection<Long> nodesIds = Collections2.transform(experimenterPojos, FunctionsUtil.toPojoId);

        // -- (ii) lookup annotations linked to model objects
        Map<Long, Collection<AnnotationData>> linkedAnnotationsMap =
                getAnnotationService().listAnnotationsLinkedToNodes(
                        experimenterId,
                        nodesIds,
                        annotatedType.getModelClass(),
                        annotationType.getModelClass());

        log.debug("Found {} annotations of type {} linked to nodes of type {} for experimenter {}",
                  linkedAnnotationsMap.size(), annotationType, annotatedType, experimenterId);

        // -- (iii) re-wrap the pojos + annotations (if any) into the internal representation
        if (null != linkedAnnotationsMap && !linkedAnnotationsMap.isEmpty()) {

            for (PojoData pojo : experimenterPojos) {
                Long id = pojo.getId();
                Collection<AnnotationData> nodeAnnotations = linkedAnnotationsMap.get(id);

                PojoData annotatedPojo = DefaultPojoData.fromAnnotatedPojo(pojo, nodeAnnotations);
                result.add(annotatedPojo);
            }

        } else {
            // no annotations linked to pojos - pass through
            result.addAll(experimenterPojos);
        }

        return result;
    }

    private void rejectIfEmpty(
            ContainerType containerType,
            AnnotatedType annotatedType,
            Long containerId,
            Collection<PojoData> pojos) {

        if (null == pojos || pojos.isEmpty()) {
            throw new IllegalStateException(String.format(
                "No %s records found within container %s of type %s "
                + "- check that your data selection and input parameters match",
                annotatedType, containerId, containerType));
        }
    }

    /**
     * Returns containerService.
     * @return the containerService
     */
    public OmeroContainerService getContainerService() {
        return containerService;
    }

    /**
     * Sets containerService.
     * @param containerService the containerService to set
     */
    public void setContainerService(OmeroContainerService containerService) {
        this.containerService = containerService;
    }

    /**
     * Returns annotationService.
     * @return the annotationService
     */
    public OmeroAnnotationService getAnnotationService() {
        return annotationService;
    }

    /**
     * Sets annotationService.
     * @param annotationService the annotationService to set
     */
    public void setAnnotationService(OmeroAnnotationService annotationService) {
        this.annotationService = annotationService;
    }

}
