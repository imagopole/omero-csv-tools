/**
 *
 */
package org.imagopole.omero.tools.impl.ctrl;

import java.util.Collection;

import com.google.common.collect.Multimap;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.ctrl.CsvAnnotationController;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.api.logic.CsvAnnotationService;
import org.imagopole.omero.tools.api.logic.CsvReaderService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher layer to the annotation/metadata related services (read-write).
 *
 * @author seb
 *
 */
public class DefaultCsvAnnotationController implements CsvAnnotationController {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvAnnotationController.class);

    /** CSV parsing service for original file conversion */
    private CsvReaderService csvReaderService;

    /** CSV annotation service for csv processing */
    private CsvAnnotationService csvAnnotationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public LinksData buildAnnotationsByTypes(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(csvData, "csvData");

        LinksData result = null;

        switch(annotationType) {

            case tag:
                result = buildTagsByAnnotatedTypeWithinContainer(
                            experimenterId, containerId, containerType, annotatedType, csvData);
                break;

            default:
                throw new UnsupportedOperationException(
                          "Other annotation types than tags not implemented");

        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IObject> saveAllAnnotationLinks(LinksData linksData) throws ServerError {
        Check.notNull(linksData, "linksData");

        return getCsvAnnotationService().saveAllAnnotationLinks(linksData);
    }

    private LinksData buildTagsByAnnotatedTypeWithinContainer(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    AnnotatedType annotatedType,
                    CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(csvData, "csvData");

        LinksData result = null;

        switch(annotatedType) {

            case dataset:
                result = linkTagsToDatasetsWithinProject(
                            experimenterId, containerId, containerType, csvData);
                break;

            case plate:
                result = linkTagsToPlatesWithinScreen(
                            experimenterId, containerId, containerType, csvData);
                break;

            case platerun:
                result = linkTagsToPlateAcquisitionsWithinPlate(
                            experimenterId, containerId, containerType, csvData);
                break;

            case image:
                result = linkTagsToImagesWithinSupportedContainerType(
                            experimenterId, containerId, containerType, csvData);
                break;

            default:
                throw new UnsupportedOperationException(
                          "Tagging other types than datasets/images "
                        + "or plate/plateacquisition not implemented");

        }

        return result;
    }

    private LinksData linkTagsToImagesWithinSupportedContainerType(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(csvData, "csvData");

        LinksData result = null;

        switch(containerType) {

            case dataset:
            case plate:
            case platerun:
                result = linkTagsToImagesWithinContainer(
                            experimenterId, containerId, containerType, csvData);
                break;

            default:
                throw new UnsupportedOperationException(
                          "Tagging within other containers than datasets "
                        + "or plates/plateacquisitions not implemented");

        }

        return result;
    }

    private LinksData linkTagsToDatasetsWithinProject(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(csvData, "csvData");

        // read and de-duplicate csv file
        Multimap<String, String> uniqueLines = parseCsvData(containerId, containerType, csvData);

        // convert csv file into bulk annotations
        LinksData linksData = getCsvAnnotationService().saveTagsAndLinkNestedDatasets(
                experimenterId,
                containerId,
                uniqueLines);

        return linksData;
    }

    private LinksData linkTagsToPlatesWithinScreen(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(csvData, "csvData");

        // read and de-duplicate csv file
        Multimap<String, String> uniqueLines = parseCsvData(containerId, containerType, csvData);

        // convert csv file into bulk annotations
        LinksData linksData =
            getCsvAnnotationService().saveTagsAndLinkNestedPlates(
                    experimenterId,
                    containerId,
                    uniqueLines);

        return linksData;
    }

    private LinksData linkTagsToPlateAcquisitionsWithinPlate(
            Long experimenterId,
            Long containerId,
            ContainerType containerType,
            CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(containerType, "containerType");
        Check.notNull(csvData, "csvData");

        // read and de-duplicate csv file
        Multimap<String, String> uniqueLines = parseCsvData(containerId, containerType, csvData);

        // convert csv file into bulk annotations
        LinksData linksData =
            getCsvAnnotationService().saveTagsAndLinkNestedPlateAcquisitions(
                    experimenterId,
                    containerId,
                    uniqueLines);

        return linksData;
    }

    private LinksData linkTagsToImagesWithinContainer(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "datasetId");
        Check.notNull(containerType, "containerType");
        Check.notNull(csvData, "csvData");

        // read and de-duplicate csv file
        Multimap<String, String> uniqueLines = parseCsvData(containerId, containerType, csvData);

        // convert csv file into bulk annotations
        LinksData linksData =
            getCsvAnnotationService().saveTagsAndLinkNestedImages(
                    experimenterId,
                    containerId,
                    containerType,
                    uniqueLines);

        return linksData;
    }

    private Multimap<String, String> parseCsvData(
            Long containerId,
            ContainerType containerType,
            CsvData csvData) {

        final String csvContent = csvData.getFileContent();
        rejectIfEmptyFile(containerType, containerId, csvContent);

        // massage input csv file into shape:
        // - merge duplicate line keys (ie. datasets/plates/images/etc. names)
        // - merge duplicate column values (ie. tags names)
        Multimap<String, String> uniqueLines = getCsvReaderService().readUniqueRecords(csvContent);

        return uniqueLines;
    }

    private void rejectIfEmptyFile(ContainerType containerType, Long containerId, String csvContent) {
        if (null == csvContent || csvContent.isEmpty()) {

            log.warn("No csv data for container {} of type {}", containerId, containerType);

            throw new IllegalStateException(String.format(
                "Empty csv content for container: %d of type %s", containerId, containerType));

        }
    }

    /**
     * @return the csvAnnotationService
     */
    public CsvReaderService getCsvReaderService() {
        return csvReaderService;
    }

    /**
     * @param csvAnnotationService the csvAnnotationService to set
     */
    public void setCsvReaderService(CsvReaderService csvAnnotationService) {
        this.csvReaderService = csvAnnotationService;
    }

    /**
     * @return the csvAnnotationService
     */
    public CsvAnnotationService getCsvAnnotationService() {
        return csvAnnotationService;
    }

    /**
     * @param csvAnnotationService the csvAnnotationService to set
     */
    public void setCsvAnnotationService(CsvAnnotationService csvAnnotationService) {
        this.csvAnnotationService = csvAnnotationService;
    }

}
