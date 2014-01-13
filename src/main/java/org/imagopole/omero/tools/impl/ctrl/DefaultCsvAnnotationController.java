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
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(annotationType, "annotationType");
        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(csvData, "csvData");

        LinksData result = null;

        // TODO: also take the ContainerType into account to allow annotations of data spanning more
        // than one container hierarchy (eg. tag images within project or dataset, or tag datasets
        // either within project or across group)
        switch(annotationType) {

            case tag:
                result =
                    buildTagsByAnnotatedType(experimenterId, containerId, annotatedType, csvData);
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

    private LinksData buildTagsByAnnotatedType(
                    Long experimenterId,
                    Long containerId,
                    AnnotatedType annotatedType,
                    CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(containerId, "containerId");
        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(csvData, "csvData");

        LinksData result = null;

        switch(annotatedType) {

            case dataset:
                result = linkTagsToDatasetsWithinProject(experimenterId, containerId, csvData);
                break;

            case image:
                // also check for the container type? this would allow to tag images both within
                // project and dataset. For now, assume tagging happens per dataset.
                result = linkTagsToImagesWithinDataset(experimenterId, containerId, csvData);
                break;

            default:
                throw new UnsupportedOperationException(
                          "Tagging other containers than datasets/images not implemented");

        }

        return result;
    }

    private LinksData linkTagsToDatasetsWithinProject(
            Long experimenterId,
            Long projectId,
            CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(projectId, "projectId");
        Check.notNull(csvData, "csvData");

        final String csvContent = csvData.getFileContent();
        rejectIfEmptyFile(ContainerType.project, projectId, csvContent);

        // massage input csv file into shape:
        // - merge duplicate line keys (ie. datasets names)
        // - merge duplicate column values (ie. tags names)
        Multimap<String, String> uniqueLines = getCsvReaderService().readUniqueRecords(csvContent);

        // convert csv file into bulk annotations
        LinksData linksData = getCsvAnnotationService().saveTagsAndLinkNestedDatasets(
                experimenterId,
                projectId,
                uniqueLines);

        return linksData;
    }

    private LinksData linkTagsToImagesWithinDataset(
                    Long experimenterId,
                    Long datasetId,
                    CsvData csvData) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(datasetId, "datasetId");
        Check.notNull(csvData, "csvData");

        final String csvContent = csvData.getFileContent();
        rejectIfEmptyFile(ContainerType.dataset, datasetId, csvContent);

        // massage input csv file into shape:
        // - merge duplicate line keys (ie. datasets names)
        // - merge duplicate column values (ie. tags names)
        Multimap<String, String> uniqueLines = getCsvReaderService().readUniqueRecords(csvContent);

        // convert csv file into bulk annotations
        LinksData linksData = getCsvAnnotationService().saveTagsAndLinkNestedImages(
                        experimenterId, datasetId, uniqueLines);

        return linksData;
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
