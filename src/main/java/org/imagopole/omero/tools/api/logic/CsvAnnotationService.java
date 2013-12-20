package org.imagopole.omero.tools.api.logic;

import java.util.Collection;

import com.google.common.collect.Multimap;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.dto.LinksData;

public interface CsvAnnotationService {

    /**
     * Builds a list of OMERO model entities based on the CSV file content:
     *  - <code>Dataset<code> entities.
     *  - <code>TagAnnotation</code> entities.
     *
     * Currently tagging is nested - ie. tags are applied to datasets *within* the
     * specified project. In the future users might wish to tag across all their datasets, group-wide?
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

    Collection<IObject> saveAllAnnotationLinks(LinksData linksData) throws ServerError;

}
