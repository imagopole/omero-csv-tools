package org.imagopole.omero.tools.api.logic;

import java.util.Collection;

import com.google.common.collect.Multimap;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.LinksData;

/**
 * Service layer to the annotations processing application logic.
 *
 * @author seb
 *
 */
public interface CsvAnnotationService {

    /**
     * Create tags based on the CSV file content and build a list of tagged OMERO <code>Dataset</code>
     * link entities for update.
     *
     * Currently tagging is hierarchical - ie. tags are applied to datasets *within* the
     * specified project.
     *
     * @param experimenterId the experimenter
     * @param projectId the project to which datasets must belong
     * @param uniqueLines the CSV line data in multivalue map format with key=dataset name and
     *        values = list of tags names. Both datasets names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @return the link model entities to be persisted, split into two sublists: one for new tags
     *         (created by this method) and one for known (existing) tags.
     * @throws ServerError OMERO client or server failure
     */
    LinksData saveTagsAndLinkNestedDatasets(
                    Long experimenterId,
                    Long projectId,
                    Multimap<String, String> uniqueLines) throws ServerError;

    /**
     * Create tags based on the CSV file content and build a list of tagged OMERO <code>Image</code>
     * link entities for update.
     *
     * Currently tagging is hierarchical - ie. tags are applied to images *within* the
     * specified container.
     *
     * @param experimenterId the experimenter
     * @param containerType the parent container for the images (eg. dataset, plate, plate acquisition)
     * @param uniqueLines the CSV line data in multivalue map format with key=image name and
     *        values = list of tags names. Both container names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @param containerId the container to which images must belong
     * @return the link model entities to be persisted, split into two sublists: one for new tags
     *         (created by this method) and one for known (existing) tags.
     * @throws ServerError OMERO client or server failure
     */
    LinksData saveTagsAndLinkNestedImages(
                    Long experimenterId,
                    Long containerId,
                    ContainerType containerType,
                    Multimap<String, String> uniqueLines) throws ServerError;

    /**
     *
     * Create tags based on the CSV file content and build a list of tagged OMERO <code>Plate</code>
     * link entities for update.
     *
     * Currently tagging is hierarchical - ie. tags are applied to plates *within* the
     * specified screen.
     *
     * @param experimenterId the experimenter
     * @param screenId the screen to which plates must belong
     * @param uniqueLines the CSV line data in multivalue map format with key=plate name and
     *        values = list of tags names. Both plates names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @return the link model entities to be persisted, split into two sublists: one for new tags
     *         (created by this method) and one for known (existing) tags.
     * @throws ServerError OMERO client or server failure
     */
    LinksData saveTagsAndLinkNestedPlates(
            Long experimenterId,
            Long screenId,
            Multimap<String, String> uniqueLines) throws ServerError;

    /**
     * Create tags based on the CSV file content and build a list of tagged OMERO <code>PlateAcquisition</code>
     * link entities for update.
     *
     * Currently tagging is hierarchical - ie. tags are applied to plateacquisitions *within* the
     * specified plate.
     *
     * @param experimenterId the experimenter
     * @param plateId the plate to which plateacquisitions must belong
     * @param uniqueLines the CSV line data in multivalue map format with key=plateacquisition name and
     *        values = list of tags names. Both plateacquisition names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @return the link model entities to be persisted, split into two sublists: one for new tags
     *         (created by this method) and one for known (existing) tags.
     * @throws ServerError OMERO client or server failure
     */
    LinksData saveTagsAndLinkNestedPlateAcquisitions(
            Long experimenterId,
            Long plateId,
            Multimap<String, String> uniqueLines) throws ServerError;

    /**
     * Persist the associations between annotations and model objects.
     *
     * @param linksData the link model entities to be persisted
     * @return the persisted model entities
     * @throws ServerError OMERO client or server failure
     */
    Collection<IObject> saveAllAnnotationLinks(LinksData linksData) throws ServerError;

}
