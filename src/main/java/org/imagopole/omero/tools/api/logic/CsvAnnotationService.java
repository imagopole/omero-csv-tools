package org.imagopole.omero.tools.api.logic;

import java.util.Collection;

import com.google.common.collect.Multimap;

import omero.ServerError;
import omero.model.IObject;

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
     * specified dataset.
     *
     * @param experimenterId the experimenter
     * @param projectId the project to which images must belong
     * @param uniqueLines the CSV line data in multivalue map format with key=image name and
     *        values = list of tags names. Both datasets names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @return the link model entities to be persisted, split into two sublists: one for new tags
     *         (created by this method) and one for known (existing) tags.
     * @throws ServerError OMERO client or server failure
     */
    LinksData saveTagsAndLinkNestedImages(
                    Long experimenterId,
                    Long datasetId,
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
